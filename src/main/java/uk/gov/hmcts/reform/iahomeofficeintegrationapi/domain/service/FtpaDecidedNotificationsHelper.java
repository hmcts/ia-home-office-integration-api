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
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.DecideFtpaApplicationOutcomeType;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.FtpaDecisionOutcomeType;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.FtpaResidentJudgeDecisionOutcomeType;


@Slf4j
@Component
public class FtpaDecidedNotificationsHelper {

    public static final String GRANTED = "granted";
    public static final String PARTIALLY_GRANTED = "partiallyGranted";
    public static final String REFUSED = "refused";
    private static final String FAIL_STATUS = "FAIL";
    private final FeatureToggler featureToggler;

    public FtpaDecidedNotificationsHelper(FeatureToggler featureToggler) {
        this.featureToggler = featureToggler;
    }

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
                notificationStatus = FAIL_STATUS;
            }

            if (!notificationStatus.equals(FAIL_STATUS)) {
                ftpaOutcome = getFtpaOutcome(ftpaAppealDecision, judgePrefix);
            }

            if (!StringUtils.hasText(notificationStatus)) {
                String note = getNote(asylumCase, ftpaApplicantType, ftpaAppealDecision, judgePrefix);

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
                logError(COURT_OUTCOME.name(), caseId, homeOfficeReferenceNumber, notificationStatus, event);
            }
        } catch (Exception e) {
            logError(COURT_OUTCOME.name(), caseId, homeOfficeReferenceNumber, notificationStatus, event);
            notificationStatus = FAIL_STATUS;
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

        return switch (ftpaAppealDecision) {
            case GRANTED, PARTIALLY_GRANTED -> Outcome.GRANTED;
            case REFUSED, "notAdmitted" -> Outcome.REFUSED;
            default -> throw new IllegalStateException("Invalid FTPA appeal decision: " + ftpaAppealDecision);
        };
    }

    private String getResidentJudgeNoteId(AsylumCase asylumCase, String ftpaApplicantType, String ftpaAppealDecision) {

        return switch (ftpaAppealDecision) {
            case "reheardRule32", "reheardRule35" -> "REHEARD_" + ftpaApplicantType.toUpperCase();
            case "remadeRule32" -> {
                String remadeDecision = asylumCase.read(valueOf(format("FTPA_%s_DECISION_REMADE_RULE_32",
                        ftpaApplicantType.toUpperCase())), String.class).orElse("");
                yield "REMADE_" + remadeDecision.toUpperCase();
            }
            case GRANTED, PARTIALLY_GRANTED, REFUSED -> {
                boolean isDlrmSetAsideEnabled = featureToggler.getValue("dlrm-setaside-feature-flag", true);
                yield (isDlrmSetAsideEnabled ?
                        DecideFtpaApplicationOutcomeType.from(ftpaAppealDecision) :
                        FtpaResidentJudgeDecisionOutcomeType.from(ftpaAppealDecision))
                        .name() + "_" + ftpaApplicantType.toUpperCase();
            }
            default -> throw new IllegalStateException("Unexpected FTPA appeal decision value: " + ftpaAppealDecision);
        };
    }

    private Outcome getResidentJudgeFtpaOutcome(final String ftpaAppealDecision) {

        return switch (ftpaAppealDecision) {
            case GRANTED, PARTIALLY_GRANTED -> Outcome.GRANTED;
            case REFUSED, "notAdmitted" -> Outcome.REFUSED;
            case "reheardRule35", "reheardRule32" -> Outcome.REHEARD;
            case "remadeRule32" -> Outcome.REMADE;
            default -> throw new IllegalStateException("Invalid FTPA appeal decision: " + ftpaAppealDecision);
        };
    }

    private Outcome getFtpaOutcome(String ftpaAppealDecision, String judgePrefix) {
        return judgePrefix.isEmpty() ? getLeadershipJudgeFtpaOutcome(ftpaAppealDecision) : getResidentJudgeFtpaOutcome(ftpaAppealDecision);
    }

    private String getNote(AsylumCase asylumCase, String ftpaApplicantType, String ftpaAppealDecision, String judgePrefix) {
        return judgePrefix.isEmpty()
                ? FtpaAppealDecidedNote.valueOf(getLeadershipJudgeNoteId(ftpaApplicantType, ftpaAppealDecision)).getValue()
                : FtpaAppealDecidedNote.valueOf(getResidentJudgeNoteId(asylumCase, ftpaApplicantType, ftpaAppealDecision)).getValue();
    }

    private void logError(String messageType, String caseId, String homeOfficeReferenceNumber, String status, Event event) {
        log.error("Failed to send {} notification for caseId: {}, HomeOffice reference: {}, status: {}, event: {}",
                messageType, caseId, homeOfficeReferenceNumber, status, event);
    }
}
