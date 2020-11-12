package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.MessageType.HEARING;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HearingInstructMessage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.HomeOfficeInstructService;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.ListingNotificationHelper;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.NotificationsHelper;

@Slf4j
@Component
public class ListCaseNotificationHandler implements PreSubmitCallbackHandler<AsylumCase> {

    protected final HomeOfficeInstructService homeOfficeInstructService;
    protected final NotificationsHelper notificationsHelper;
    protected final ListingNotificationHelper listingNotificationHelper;

    public ListCaseNotificationHandler(
        HomeOfficeInstructService homeOfficeInstructService,
        NotificationsHelper notificationsHelper,
        ListingNotificationHelper listingNotificationHelper) {
        this.homeOfficeInstructService = homeOfficeInstructService;
        this.notificationsHelper = notificationsHelper;
        this.listingNotificationHelper = listingNotificationHelper;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && (callback.getEvent() == Event.LIST_CASE);
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        log.info("Preparing to send {} notification to HomeOffice for event {}",
            HEARING.toString(), callback.getEvent());

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        final String homeOfficeReferenceNumber = notificationsHelper.getHomeOfficeReference(asylumCase);

        final String caseId = notificationsHelper.getCaseId(asylumCase);

        final HearingInstructMessage hearingInstructMessage
            = listingNotificationHelper.getHearingInstructMessage(
                asylumCase, notificationsHelper.getConsumerReference(caseId),
                notificationsHelper.getMessageHeader(), homeOfficeReferenceNumber);

        log.info("Finished constructing {} notification request for caseId: {}, HomeOffice reference: {}",
            HEARING.toString(), caseId, homeOfficeReferenceNumber);

        String notificationStatus = homeOfficeInstructService.sendNotification(hearingInstructMessage);

        log.info("SENT: {} notification for caseId: {}, HomeOffice reference: {}, status: {}, event: {}",
            HEARING.toString(), caseId, homeOfficeReferenceNumber, notificationStatus, callback.getEvent());

        asylumCase.write(AsylumCaseDefinition.HOME_OFFICE_HEARING_INSTRUCT_STATUS, notificationStatus);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
