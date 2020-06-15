package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_INSTRUCT_STATUS;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.State.APPEAL_SUBMITTED;

import java.util.Collections;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.MessageHeader;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.HomeOfficeInstructService;

@Component
public class AsylumCaseNotificationHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private HomeOfficeInstructService homeOfficeInstructService;

    public AsylumCaseNotificationHandler(HomeOfficeInstructService homeOfficeInstructService) {
        this.homeOfficeInstructService = homeOfficeInstructService;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.SUBMIT_APPEAL
            && callback.getCaseDetails().getState() == APPEAL_SUBMITTED;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        final String homeOfficeReferenceNumber = asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)
            .orElseThrow(() -> new IllegalStateException("Home office reference for the appeal is not present"));

        final String caseId = asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)
            .orElseThrow(() -> new IllegalStateException("Case ID for the appeal is not present"));

        MessageHeader messageHeader = homeOfficeInstructService.sendNotification(homeOfficeReferenceNumber,caseId);
        PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);
        if (messageHeader == null) {
            asylumCase.write(HOME_OFFICE_INSTRUCT_STATUS,"Error sending notification to Home Office");
            response.addErrors(Collections.singleton("Error sending notification to Home Office"));
        } else {
            asylumCase.write(HOME_OFFICE_INSTRUCT_STATUS,"Notification sent to Home Office");
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

}
