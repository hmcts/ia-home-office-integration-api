package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.MessageType.PERMISSION_TO_APPEAL;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.PermissionToAppealInstructMessage.PermissionToAppealInstructMessageBuilder.permissionToAppealInstructMessage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.CourtType;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.PermissionToAppealInstructMessage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.HomeOfficeInstructService;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.NotificationsHelper;


@Slf4j
@Component
public class FtpaAppellantNotificationHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final HomeOfficeInstructService homeOfficeInstructService;
    private final NotificationsHelper notificationsHelper;

    public FtpaAppellantNotificationHandler(
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
               && (callback.getEvent() == Event.APPLY_FOR_FTPA_APPELLANT);
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }
        log.info("Preparing to send {} notification to HomeOffice for event: {}",
            PERMISSION_TO_APPEAL.toString(), callback.getEvent());

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        final String homeOfficeReferenceNumber = notificationsHelper.getHomeOfficeReference(asylumCase);

        final String caseId = asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)
            .orElseThrow(() -> new IllegalStateException("Case ID for the appeal is not present"));

        final PermissionToAppealInstructMessage bundleInstructMessage =
            permissionToAppealInstructMessage()
                .withConsumerReference(notificationsHelper.getConsumerReference(caseId))
                .withHoReference(homeOfficeReferenceNumber)
                .withMessageHeader(notificationsHelper.getMessageHeader())
                .withMessageType(PERMISSION_TO_APPEAL.name())
                .withCourtType(CourtType.FIRST_TIER)
                .withNote(
                    "The appellant has submitted an application for permission to appeal to the Upper Tribunal.\n"
                    + "Next steps\n"
                    + "The First-tier Tribunal will consider the application and make a decision shortly. "
                    + "You will be informed of the outcome.")
                .build();

        log.info("Finished constructing {} notification request for caseId: {}, HomeOffice reference: {}, Event: {}",
            PERMISSION_TO_APPEAL.toString(), caseId, homeOfficeReferenceNumber, callback.getEvent());

        final String notificationStatus = homeOfficeInstructService.sendNotification(bundleInstructMessage);

        asylumCase.write(AsylumCaseDefinition.HOME_OFFICE_FTPA_APPELLANT_INSTRUCT_STATUS, notificationStatus);

        log.info("SENT: {} notification for caseId: {}, HomeOffice reference: {}, status: {}, Event: {}",
            PERMISSION_TO_APPEAL.toString(), caseId, homeOfficeReferenceNumber, notificationStatus,
            callback.getEvent());

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
