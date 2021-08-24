package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPEAL_OUT_OF_COUNTRY;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPELLANT_DATE_OF_BIRTH;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPELLANT_FULL_NAME;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_API_ERROR;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_APPELLANTS_LIST;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER_BEFORE_EDIT;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_SEARCH_RESPONSE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS_MESSAGE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.MATCHING_APPELLANT_DETAILS_FOUND;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event.REQUEST_HOME_OFFICE_DATA;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event.REQUEST_HOME_OFFICE_DATA;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.HomeOfficeDataErrorsHelper;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.HomeOfficeDataMatchHelper;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeCaseStatus;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeSearchResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.Person;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.Person.PersonBuilder;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.DynamicList;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Value;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.HomeOfficeSearchService;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.HomeOfficeResponseException;

@Slf4j
@Component
public class RequestHomeOfficeDataPreparer implements PreSubmitCallbackHandler<AsylumCase> {

    private static final String PROBLEM_MESSAGE = "### There is a problem\n\n";

    private static final String HOME_OFFICE_CALL_ERROR_MESSAGE = PROBLEM_MESSAGE
            + "The service has been unable t"
            + "o retrieve the Home Office information about this appeal.\n\n"
            + "[Request the Home Office information](/case/IA/Asylum/${[CASE_REFERENCE]}/"
            + "trigger/requestHomeOfficeData) to try again. This may take a few minutes.";

    private static final String INVALID_HOME_OFFICE_REFERENCE = "The Home office does not recognise the submitted "
            + "appellant reference";

    private final FeatureToggler featureToggler;

    private HomeOfficeSearchService homeOfficeSearchService;

    private HomeOfficeDataErrorsHelper homeOfficeDataErrorsHelper;

    private HomeOfficeDataMatchHelper homeOfficeDataMatchHelper;

    public RequestHomeOfficeDataPreparer(HomeOfficeSearchService homeOfficeSearchService,
                                         HomeOfficeDataErrorsHelper homeOfficeDataErrorsHelper,
                                         HomeOfficeDataMatchHelper homeOfficeDataMatchHelper,
                                         FeatureToggler featureToggler) {

        this.homeOfficeSearchService = homeOfficeSearchService;
        this.homeOfficeDataErrorsHelper = homeOfficeDataErrorsHelper;
        this.homeOfficeDataMatchHelper = homeOfficeDataMatchHelper;
        this.featureToggler = featureToggler;
    }

    @Override
    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
                && callback.getEvent() == REQUEST_HOME_OFFICE_DATA
                && featureToggler.getValue("home-office-uan-feature", false);
    }


    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        final long caseId = callback.getCaseDetails().getId();


        final YesOrNo isAppealOutOfCountry = asylumCase.read(APPEAL_OUT_OF_COUNTRY, YesOrNo.class).orElse(YesOrNo.NO);

        if (isAppealOutOfCountry == YesOrNo.YES) {

            PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);
            response.addError("You cannot request Home Office data for an out of country appeal");
            return response;
        }


        final String homeOfficeReferenceNumber = asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)
                .orElseThrow(() -> new IllegalStateException(
                        "Home office reference for the appeal is not present, caseId: " + caseId));

        final String appellantDateOfBirth = asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)
                .orElseThrow(() -> new IllegalStateException("Appellant date of birth is not present."));

        final String appellantGiveName = asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse("");
        final String appellantFamilyName = asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse("");

        final PersonBuilder appellant =
                PersonBuilder.person()
                        .withGivenName(appellantGiveName)
                        .withFamilyName(appellantFamilyName);

        asylumCase.write(APPELLANT_FULL_NAME, appellantGiveName + " " + appellantFamilyName);

        HomeOfficeSearchResponse searchResponse;
        DynamicList dynamicList = null;
        final List<Value> values = new ArrayList<>();

        try {
            String homeOfficeSearchResponseJsonStr =
                    asylumCase.read(HOME_OFFICE_SEARCH_RESPONSE, String.class).orElse("");
            String homeOfficeReferenceNumberBeforeEdit  =
                    asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER_BEFORE_EDIT, String.class).orElse("");

            if (!homeOfficeSearchResponseJsonStr.isEmpty()
                    && (homeOfficeReferenceNumberBeforeEdit.isEmpty()
                    || homeOfficeReferenceNumberBeforeEdit.equals(homeOfficeReferenceNumber))) {

                searchResponse = new ObjectMapper()
                        .readValue(homeOfficeSearchResponseJsonStr, HomeOfficeSearchResponse.class);
            } else {

                searchResponse = homeOfficeSearchService.getCaseStatus(caseId, homeOfficeReferenceNumber);
            }

            if (searchResponse.getErrorDetail() != null) {
                final String errMessage = String.format("Error code: %s, message: %s",
                        searchResponse.getErrorDetail().getErrorCode(),
                        searchResponse.getErrorDetail().getMessageText());
                homeOfficeDataErrorsHelper.setErrorMessageForErrorCode(
                        caseId,
                        asylumCase,
                        searchResponse.getErrorDetail().getErrorCode(),
                        errMessage
                );
                log.warn(
                        "Error message from Home office service, caseId: {}, error message: {}",
                        caseId,
                        errMessage
                );

                return new PreSubmitCallbackResponse<>(asylumCase);
            }

            List<HomeOfficeCaseStatus> matchedApplicants =
                    findMatchingApplicants(
                            searchResponse.getStatus(), homeOfficeReferenceNumber,
                            appellant.build(), appellantDateOfBirth);

            matchedApplicants = matchedApplicants.size() > 0
                    ? findAllApplicants(searchResponse.getStatus(), homeOfficeReferenceNumber)
                    : matchedApplicants;

            if (!matchedApplicants.isEmpty()) {

                homeOfficeSearchResponseJsonStr = new ObjectMapper().writeValueAsString(searchResponse);
                asylumCase.write(HOME_OFFICE_SEARCH_RESPONSE, homeOfficeSearchResponseJsonStr);

                matchedApplicants.stream().forEach(a -> {
                    Person person = a.getPerson();
                    String applicantDob = String.format("%02d", person.getDayOfBirth())
                            + String.format("%02d", person.getMonthOfBirth())
                            + String.valueOf(person.getYearOfBirth()).substring(2);

                    values.add(new Value(a.getPerson().getFullName(),
                            a.getPerson().getFullName() + "-" + applicantDob));
                });

                asylumCase.write(HOME_OFFICE_SEARCH_STATUS, "SUCCESS");
                asylumCase.write(MATCHING_APPELLANT_DETAILS_FOUND, YesOrNo.YES);
            } else {

                asylumCase.write(MATCHING_APPELLANT_DETAILS_FOUND, YesOrNo.NO);
            }


        } catch (HomeOfficeResponseException hoe) {
            homeOfficeDataErrorsHelper
                    .setErrorMessageForErrorCode(caseId, asylumCase, hoe.getErrorCode(), hoe.getMessage());
        } catch (Exception e) {
            log.warn(
                    "Error while calling Home office case status search: caseId: {}, error message: {}",
                    caseId,
                    e.getMessage(),
                    e)
            ;
            asylumCase.write(HOME_OFFICE_SEARCH_STATUS, "FAIL");
            asylumCase.write(HOME_OFFICE_SEARCH_STATUS_MESSAGE, HOME_OFFICE_CALL_ERROR_MESSAGE);
            asylumCase.write(HOME_OFFICE_API_ERROR, INVALID_HOME_OFFICE_REFERENCE);
        }

        values.add(new Value("NoMatch", "No Match"));
        dynamicList = new DynamicList(values.get(0), values);
        asylumCase.write(HOME_OFFICE_APPELLANTS_LIST, dynamicList);

        return new PreSubmitCallbackResponse<>(asylumCase);

    }

    List<HomeOfficeCaseStatus> findMatchingApplicants(
            List<HomeOfficeCaseStatus> statuses, String homeOfficeReferenceNumber,
            Person appellant, String appellantDob) {

        return statuses.stream()
                .filter(a ->
                        a.getApplicationStatus().getDocumentReference().contains(homeOfficeReferenceNumber)
                        && homeOfficeDataMatchHelper.isApplicantMatched(a, appellant, appellantDob))
                .collect(Collectors.toList());
    }

    List<HomeOfficeCaseStatus> findAllApplicants(
            List<HomeOfficeCaseStatus> statuses, String homeOfficeReferenceNumber) {

        return statuses.stream()
                .filter(a -> a.getApplicationStatus().getDocumentReference().contains(homeOfficeReferenceNumber))
                .collect(Collectors.toList());
    }
}
