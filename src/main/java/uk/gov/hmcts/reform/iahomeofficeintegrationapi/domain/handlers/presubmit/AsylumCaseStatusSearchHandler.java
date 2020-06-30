package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.DecisionStatus;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeSearchResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.Person;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.HomeOfficeSearchService;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.util.HomeOfficeDateFormatter;

@Component
public class AsylumCaseStatusSearchHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private HomeOfficeSearchService homeOfficeSearchService;

    public AsylumCaseStatusSearchHandler(HomeOfficeSearchService homeOfficeSearchService) {
        this.homeOfficeSearchService = homeOfficeSearchService;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.SUBMIT_APPEAL
            && callback.getCaseDetails().getState() == State.APPEAL_SUBMITTED;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        final String homeOfficeReferenceNumber = asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)
            .orElseThrow(() -> new IllegalStateException("Home office reference for the appeal is not present"));

        HomeOfficeSearchResponse searchResponse = homeOfficeSearchService.getCaseStatus(homeOfficeReferenceNumber);
        if (searchResponse.getStatus() == null
            || searchResponse.getStatus().isEmpty()
            || searchResponse.getStatus().get(0).getPerson() == null
            || searchResponse.getStatus().get(0).getDecisionStatus() == null) {
            asylumCase.write(AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS, "FAIL");
        } else {
            Person person = searchResponse.getStatus().get(0).getPerson();
            DecisionStatus decisionStatus = searchResponse.getStatus().get(0).getDecisionStatus();
            asylumCase.write(AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS, "SUCCESS");
            asylumCase.write(AsylumCaseDefinition.HO_APPELLANT_GIVEN_NAME, person.getGivenName());
            asylumCase.write(AsylumCaseDefinition.HO_APPELLANT_FAMILY_NAME, person.getFamilyName());
            asylumCase.write(AsylumCaseDefinition.HO_APPELLANT_FULL_NAME, person.getFullName());
            asylumCase.write(AsylumCaseDefinition.HO_APPELLANT_NATIONALITY, person.getNationality().getDescription());
            asylumCase.write(
                AsylumCaseDefinition.HO_APPLICATION_DECISION, decisionStatus.getDecisionType().getDescription());
            asylumCase.write(
                AsylumCaseDefinition.HO_APPLICATION_DECISION_DATE,
                HomeOfficeDateFormatter.getIacDecisionDate(decisionStatus.getDecisionDate()));
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

}
