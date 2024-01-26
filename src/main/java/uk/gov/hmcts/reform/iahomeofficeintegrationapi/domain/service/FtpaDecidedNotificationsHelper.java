package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AppealDecidedInstructMessage.AppealDecidedInstructMessageBuilder.appealDecidedInstructMessage;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.FTPA_APPLICANT_TYPE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.valueOf;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.MessageType.COURT_OUTCOME;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AppealDecidedInstructMessage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.CourtOutcome;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.CourtType;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.Outcome;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.DecideFtpaApplicationDecisionOutcomeType;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.FtpaDecisionOutcomeType;


@Slf4j
@Component
public class FtpaDecidedNotificationsHelper {


    public static final String GRANTED = "granted";
    public static final String PARTIALLY_GRANTED = "partiallyGranted";
    public static final String REFUSED = "refused";

    public String handleFtpaDecidedNotification(
        AsylumCase asylumCase,
        NotificationsHelper notificationsHelper,
        HomeOfficeInstructService homeOfficeInstructService,
        Event event,
        String judgePrefix
    ) {

        String ftpaApplicantType = "";
        String notificationStatus = "";
        String caseId = null;
        String homeOfficeReferenceNumber = null;
        try {

            log.info("Preparing to send {} notification to HomeOffice for event {}",
                COURT_OUTCOME.name(), event);

            ftpaApplicantType = asylumCase
                .read(FTPA_APPLICANT_TYPE, String.class)
                .orElseThrow(() -> new IllegalStateException("FtpaApplicantType is not present"));

            homeOfficeReferenceNumber = notificationsHelper.getHomeOfficeReference(asylumCase);

            caseId = asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)
                .orElseThrow(() -> new IllegalStateException("Case ID for the appeal is not present"));

            Outcome ftpaOutcome = null;
            String ftpaAppealDecision = null;


            if (ftpaApplicantType.equals("appellant")) {
                ftpaAppealDecision = asylumCase.read(valueOf(format("FTPA_APPELLANT_%sDECISION_OUTCOME_TYPE",
                    judgePrefix.toUpperCase())), String.class)
                    .orElse("");

            } else if (ftpaApplicantType.equals("respondent")) {

                ftpaAppealDecision = asylumCase.read(valueOf(format("FTPA_RESPONDENT_%sDECISION_OUTCOME_TYPE",
                    judgePrefix.toUpperCase())), String.class)
                    .orElse("");

            } else {

                log.error("Invalid applicant type: {} while sending : "
                          + "{} notification for caseId: {}, HomeOffice reference: {}, status: {}, event: {}",
                    ftpaApplicantType, COURT_OUTCOME.name(), caseId, homeOfficeReferenceNumber, notificationStatus,
                    event);
                notificationStatus = "FAIL";
            }

            if (!notificationStatus.equals("FAIL")) {
                ftpaOutcome = judgePrefix.isEmpty()
                    ? getLeadershipJudgeFtpaOutcome(ftpaAppealDecision)
                    : getDecideFtpaApplicationOutcome(ftpaAppealDecision);
            }

            if (StringUtils.isEmpty(notificationStatus)) {

                String note = judgePrefix.isEmpty()
                    ? FtpaAppealDecidedNote.valueOf(
                        getLeadershipJudgeNoteId(ftpaApplicantType, ftpaAppealDecision)).getValue()
                    : FtpaAppealDecidedNote.valueOf(
                        getDecideFtpaApplicationNoteId(asylumCase, ftpaApplicantType, ftpaAppealDecision)).getValue();

                final AppealDecidedInstructMessage bundleInstructMessage =
                    appealDecidedInstructMessage()
                        .withConsumerReference(notificationsHelper.getConsumerReference(caseId))
                        .withHoReference(homeOfficeReferenceNumber)
                        .withMessageHeader(notificationsHelper.getMessageHeader())
                        .withMessageType(COURT_OUTCOME.name())
                        .withCourtOutcome(new CourtOutcome(CourtType.FIRST_TIER, ftpaOutcome))
                        .withNote(note)
                        .build();

                log.info("Finished constructing {} notification request for caseId: {}, HomeOffice reference: {}",
                    COURT_OUTCOME.name(), caseId, homeOfficeReferenceNumber);

                notificationStatus = homeOfficeInstructService.sendNotification(bundleInstructMessage);

                log.info("SENT: {} notification for caseId: {}, HomeOffice reference: {}, status: {}, event: {}",
                    COURT_OUTCOME.name(), caseId, homeOfficeReferenceNumber, notificationStatus, event);
            } else {
                log.error(
                    "Failed to send {} notification for caseId: {}, HomeOffice reference: {}, status: {}, event: {}",
                    COURT_OUTCOME.name(), caseId, homeOfficeReferenceNumber, notificationStatus, event
                );
            }
        } catch (Exception e) {
            log.error("Failed to send {} notification for caseId: {}, HomeOffice reference: {}, status: {}, event: {}",
                COURT_OUTCOME.name(), caseId, homeOfficeReferenceNumber, notificationStatus, event);
            notificationStatus = "FAIL";
        }

        asylumCase.write(valueOf(format("HOME_OFFICE_FTPA_%s_DECIDED_INSTRUCT_STATUS",
            ftpaApplicantType.toUpperCase())), notificationStatus);

        return notificationStatus;
    }

    private String getLeadershipJudgeNoteId(String ftpaApplicantType, String ftpaAppealDecision) {
        return
            FtpaDecisionOutcomeType.from(ftpaAppealDecision).name() + "_" + ftpaApplicantType.toUpperCase();
    }

    private Outcome getLeadershipJudgeFtpaOutcome(final String ftpaAppealDecision) {

        Outcome ftpaOutcome;

        switch (ftpaAppealDecision) {
            case GRANTED:
            case PARTIALLY_GRANTED:
                ftpaOutcome = Outcome.GRANTED;
                break;
            case REFUSED:
                ftpaOutcome = Outcome.REFUSED;
                break;
            case "notAdmitted":
                ftpaOutcome = Outcome.REFUSED;
                break;
            default:
                throw new IllegalStateException("Invalid FTPA appeal decision: " + ftpaAppealDecision);
        }

        return ftpaOutcome;
    }

    private String getDecideFtpaApplicationNoteId(AsylumCase asylumCase, String ftpaApplicantType, String ftpaAppealDecision) {

        String noteId;

        switch (ftpaAppealDecision) {
            case "reheardRule32":
            case "reheardRule35":
                noteId = "REHEARD_" + ftpaApplicantType.toUpperCase();
                break;
            case "remadeRule32":
                String remadeDecision = asylumCase.read(valueOf(format("FTPA_%s_DECISION_REMADE_RULE_32",
                    ftpaApplicantType.toUpperCase())), String.class).orElse("");
                noteId = "REMADE_" + remadeDecision.toUpperCase();
                break;
            case GRANTED:
            case PARTIALLY_GRANTED:
            case REFUSED:
                noteId = DecideFtpaApplicationDecisionOutcomeType.from(ftpaAppealDecision).name()
                         + "_" + ftpaApplicantType.toUpperCase();
                break;
            default:
                throw new IllegalStateException("Unexpected FTPA appeal decision value: " + ftpaAppealDecision);
        }
        return noteId;
    }

    private Outcome getDecideFtpaApplicationOutcome(final String ftpaAppealDecision) {

        Outcome ftpaOutcome;

        switch (ftpaAppealDecision) {
            case GRANTED:
            case PARTIALLY_GRANTED:
                ftpaOutcome = Outcome.GRANTED;
                break;
            case REFUSED:
                ftpaOutcome = Outcome.REFUSED;
                break;
            case "notAdmitted":
                ftpaOutcome = Outcome.REFUSED;
                break;
            case "reheardRule35":
            case "reheardRule32":
                ftpaOutcome = Outcome.REHEARD;
                break;
            case "remadeRule32":
                ftpaOutcome = Outcome.REMADE;
                break;
            default:
                throw new IllegalStateException("Invalid FTPA appeal decision: " + ftpaAppealDecision);
        }

        return ftpaOutcome;
    }
}
