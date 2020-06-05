package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.HomeOfficeCaseStatusClient;

@Component
public class AsylumCaseStatusSearchHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private HomeOfficeCaseStatusClient homeOfficeCaseStatusClient;

    AsylumCaseStatusSearchHandler(HomeOfficeCaseStatusClient homeOfficeCaseStatusClient) {
        this.homeOfficeCaseStatusClient = homeOfficeCaseStatusClient;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.SUBMIT_APPEAL;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        final Optional<String> homeOfficeReferenceNumber = asylumCase.read(
            AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER);

        if (!homeOfficeReferenceNumber.isPresent()) {
            throw new IllegalStateException("Home office reference number cannot be null");
        }

        //Should POST to home office service with search parameters
        //Should return updated AsylumCase
        String homeOfficeCaseData = homeOfficeCaseStatusClient.getCaseStatus(asylumCase);
        //convert the string to case data thru a Service
        asylumCase.write(AsylumCaseDefinition.HO_APPELLANT_FAMILY_NAME, homeOfficeCaseData);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

}
