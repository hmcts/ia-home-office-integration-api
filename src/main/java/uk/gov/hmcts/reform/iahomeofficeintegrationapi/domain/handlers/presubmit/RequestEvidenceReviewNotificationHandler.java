package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.MessageType.REQUEST_REVIEW;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.RequestEvidenceReviewInstructMessage.RequestEvidenceReviewInstructMessageBuilder.requestEvidenceReviewInstructMessage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.RequestEvidenceReviewInstructMessage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.HomeOfficeInstructService;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.NotificationsHelper;


@Slf4j
@Component
public class RequestEvidenceReviewNotificationHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final HomeOfficeInstructService homeOfficeInstructService;
    private NotificationsHelper notificationsHelper;

    public RequestEvidenceReviewNotificationHandler(
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
               && (callback.getEvent() == Event.REQUEST_RESPONDENT_REVIEW);
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        log.info("Preparing to send {} notification to HomeOffice for event {}",
            REQUEST_REVIEW.toString(), callback.getEvent());

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        final String homeOfficeReferenceNumber = notificationsHelper.getHomeOfficeReference(asylumCase);

        final String caseId = asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)
            .orElseThrow(() -> new IllegalStateException("Case ID for the appeal is not present"));

        final RequestEvidenceReviewInstructMessage bundleInstructMessage =
            requestEvidenceReviewInstructMessage()
                .withConsumerReference(notificationsHelper.getConsumerReference(caseId))
                .withHoReference(homeOfficeReferenceNumber)
                .withMessageHeader(notificationsHelper.getMessageHeader())
                .withMessageType(REQUEST_REVIEW.name())
                .withNote(notificationsHelper.getDirectionContent(asylumCase, DirectionTag.RESPONDENT_REVIEW))
                .withDeadlineDate(notificationsHelper.getDirectionDeadline(asylumCase, DirectionTag.RESPONDENT_REVIEW))
                .build();

        log.info("Finished constructing {} notification request for caseId: {}, HomeOffice reference: {}",
            REQUEST_REVIEW.toString(), caseId, homeOfficeReferenceNumber);

        final String notificationStatus = homeOfficeInstructService.sendNotification(bundleInstructMessage);

        asylumCase.write(AsylumCaseDefinition.HOME_OFFICE_REQUEST_REVIEW_INSTRUCT_STATUS, notificationStatus);

        log.info("SENT: {} notification for caseId: {}, HomeOffice reference: {}, status: {}, event: {}",
            REQUEST_REVIEW.toString(), caseId, homeOfficeReferenceNumber, notificationStatus, callback.getEvent());

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
