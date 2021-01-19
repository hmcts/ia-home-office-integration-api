package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_AMEND_BUNDLE_INSTRUCT_STATUS;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.MessageType.DEFAULT;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeInstruct;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.HomeOfficeInstructService;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.NotificationsHelper;

@Slf4j
@Component
public class AmendBundleNotificationHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private HomeOfficeInstructService homeOfficeInstructService;
    private NotificationsHelper notificationsHelper;

    public AmendBundleNotificationHandler(
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
               && callback.getEvent() == Event.SEND_DIRECTION
               && callback.getCaseDetails().getState() == State.AWAITING_RESPONDENT_EVIDENCE;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }
        log.info("Preparing to send {} notification to HomeOffice for event {}",
            DEFAULT.toString(), callback.getEvent());

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        final String homeOfficeReferenceNumber = notificationsHelper.getHomeOfficeReference(asylumCase);

        final String caseId = notificationsHelper.getCaseId(asylumCase);

        final HomeOfficeInstruct homeOfficeInstruct =
            new HomeOfficeInstruct(
                notificationsHelper.getConsumerReference(caseId),
                homeOfficeReferenceNumber,
                notificationsHelper.getMessageHeader(),
                DEFAULT.name(),
                notificationsHelper.getDirectionContent(asylumCase, DirectionTag.NONE)
            );

        log.info("Finished constructing {} notification request for caseId: {}, HomeOffice reference: {}",
            DEFAULT.toString(), caseId, homeOfficeReferenceNumber);

        final String notificationStatus = homeOfficeInstructService.sendNotification(homeOfficeInstruct);

        asylumCase.write(HOME_OFFICE_AMEND_BUNDLE_INSTRUCT_STATUS, notificationStatus);

        log.info("SENT: {} notification for caseId: {}, HomeOffice reference: {}, status: {}, event: {}",
            DEFAULT.toString(), caseId, homeOfficeReferenceNumber, notificationStatus, callback.getEvent());

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
