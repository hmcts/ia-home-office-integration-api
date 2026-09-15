package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AppealDecidedInstructMessage.AppealDecidedInstructMessageBuilder.appealDecidedInstructMessage;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.IS_DECISION_ALLOWED;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.MessageType.COURT_OUTCOME;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event.SEND_DECISION_AND_REASONS;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AppealDecidedInstructMessage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AppealDecision;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.CourtOutcome;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.CourtType;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.Outcome;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.AppealDecidedNote;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.HomeOfficeInstructService;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.NotificationsHelper;


@Slf4j
@Component
public class AppealDecidedNotificationHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private HomeOfficeInstructService homeOfficeInstructService;
    private NotificationsHelper notificationsHelper;

    public AppealDecidedNotificationHandler(
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
               && (callback.getEvent() == SEND_DECISION_AND_REASONS);
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

        final String homeOfficeReferenceNumber = notificationsHelper.getHomeOfficeReference(asylumCase);

        final String caseId = asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)
            .orElseThrow(() -> new IllegalStateException("Case ID for the appeal is not present"));

        final String appealDecision = asylumCase.read(IS_DECISION_ALLOWED, AppealDecision.class)
            .orElseThrow(() -> new IllegalStateException("decision property must be set"))
            .getValue().toUpperCase();

        final AppealDecidedInstructMessage bundleInstructMessage =
            appealDecidedInstructMessage()
                .withConsumerReference(notificationsHelper.getConsumerReference(caseId))
                .withHoReference(homeOfficeReferenceNumber)
                .withMessageHeader(notificationsHelper.getMessageHeader())
                .withMessageType(COURT_OUTCOME.name())
                .withCourtOutcome(new CourtOutcome(CourtType.FIRST_TIER, Outcome.valueOf(appealDecision)))
                .withNote(AppealDecidedNote.valueOf(appealDecision).toString())
                .build();

        log.info("Finished constructing {} notification request for caseId: {}, HomeOffice reference: {}",
            COURT_OUTCOME.name(), caseId, homeOfficeReferenceNumber);

        final String notificationStatus = homeOfficeInstructService.sendNotification(bundleInstructMessage);

        asylumCase.write(AsylumCaseDefinition.HOME_OFFICE_APPEAL_DECIDED_INSTRUCT_STATUS, notificationStatus);

        log.info("SENT: {} notification for caseId: {}, HomeOffice reference: {}, status: {}, event: {}",
            COURT_OUTCOME.name(), caseId, homeOfficeReferenceNumber, notificationStatus, callback.getEvent());

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
