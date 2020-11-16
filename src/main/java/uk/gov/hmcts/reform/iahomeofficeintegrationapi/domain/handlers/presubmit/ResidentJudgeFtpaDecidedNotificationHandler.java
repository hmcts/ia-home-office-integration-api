package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.presubmit;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AppealDecidedInstructMessage.AppealDecidedInstructMessageBuilder.appealDecidedInstructMessage;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.FTPA_APPLICANT_TYPE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.valueOf;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.MessageType.COURT_OUTCOME;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event.RESIDENT_JUDGE_FTPA_DECISION;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AppealDecidedInstructMessage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.CourtOutcome;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.CourtType;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.Outcome;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.FtpaResidentJudgeDecisionOutcomeType;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.FtpaAppealDecidedNote;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.HomeOfficeInstructService;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.NotificationsHelper;



@Slf4j
@Component
public class ResidentJudgeFtpaDecidedNotificationHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private HomeOfficeInstructService homeOfficeInstructService;
    private NotificationsHelper notificationsHelper;

    public ResidentJudgeFtpaDecidedNotificationHandler(
        HomeOfficeInstructService homeOfficeInstructService,
        NotificationsHelper notificationsHelper
    ) {
        this.homeOfficeInstructService = homeOfficeInstructService;
        this.notificationsHelper = notificationsHelper;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && (callback.getEvent() == RESIDENT_JUDGE_FTPA_DECISION);
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }
        log.info("Preparing to send {} notification to HomeOffice for event {}",
            COURT_OUTCOME.name(), callback.getEvent());

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        String ftpaApplicantType = "";
        String notificationStatus = "";
        String caseId = null;
        String homeOfficeReferenceNumber = null;
        try {

            ftpaApplicantType = asylumCase
                .read(FTPA_APPLICANT_TYPE, String.class)
                .orElseThrow(() -> new IllegalStateException("FtpaApplicantType is not present"));

            homeOfficeReferenceNumber = notificationsHelper.getHomeOfficeReference(asylumCase);

            caseId = asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)
                .orElseThrow(() -> new IllegalStateException("Case ID for the appeal is not present"));

            Outcome ftpaOutcome = null;
            String ftpaAppealDecision = null;


            if (ftpaApplicantType.equals("appellant")) {

                ftpaAppealDecision = asylumCase.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, String.class)
                    .orElse("");
            } else if (ftpaApplicantType.equals("respondent")) {

                ftpaAppealDecision = asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, String.class)
                    .orElse("");
            } else {

                log.error("Invalid applicant type: {} while sending : "
                          + "{} notification for caseId: {}, HomeOffice reference: {}, status: {}, event: {}",
                    ftpaApplicantType, COURT_OUTCOME.name(), caseId, homeOfficeReferenceNumber, notificationStatus,
                    callback.getEvent());
                notificationStatus = "FAIL";
            }

            if (!notificationStatus.equals("FAIL")) {
                ftpaOutcome = getFtpaOutcome(ftpaAppealDecision);
            }

            if (StringUtils.isEmpty(notificationStatus)) {

                String note = FtpaAppealDecidedNote.valueOf(
                    getNoteId(asylumCase, ftpaApplicantType, ftpaAppealDecision)).getValue();

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
                    COURT_OUTCOME.name(), caseId, homeOfficeReferenceNumber, notificationStatus, callback.getEvent());
            } else {
                log.error(
                    "Failed to send {} notification for caseId: {}, HomeOffice reference: {}, status: {}, event: {}",
                    COURT_OUTCOME.name(), caseId, homeOfficeReferenceNumber, notificationStatus, callback.getEvent()
                );
            }
        } catch (Exception e) {
            log.error("Failed to send {} notification for caseId: {}, HomeOffice reference: {}, status: {}, event: {}",
                COURT_OUTCOME.name(), caseId, homeOfficeReferenceNumber, notificationStatus, callback.getEvent());
            log.error("Exception: {}", e.getMessage());
            notificationStatus = "FAIL";
        }

        asylumCase.write(valueOf(format("HOME_OFFICE_FTPA_%s_DECIDED_INSTRUCT_STATUS",
            ftpaApplicantType.toUpperCase())), notificationStatus);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private String getNoteId(AsylumCase asylumCase, String ftpaApplicantType, String ftpaAppealDecision) {

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
            case "granted":
            case "partiallyGranted":
            case "refused":
                noteId = FtpaResidentJudgeDecisionOutcomeType.from(ftpaAppealDecision).name()
                         + "_" + ftpaApplicantType.toUpperCase();
                break;
            default:
                throw new IllegalStateException("Unexpected FTPA appeal decision value: " + ftpaAppealDecision);
        }
        return noteId;
    }

    private Outcome getFtpaOutcome(final String ftpaAppealDecision) {

        Outcome ftpaOutcome;

        switch (ftpaAppealDecision) {
            case "granted":
            case "partiallyGranted":
                ftpaOutcome = Outcome.GRANTED;
                break;
            case "refused":
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
