package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPELLANT_FULL_NAME;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_APPELLANTS_LIST;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS_MESSAGE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.MATCHING_APPELLANT_DETAILS_FOUND;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.Person.PersonBuilder.person;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event.REQUEST_HOME_OFFICE_DATA;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.HomeOfficeDataErrorsHelper;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeCaseStatus;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeSearchResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.Person;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.DynamicList;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Value;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.PreSubmitCallbackHandler;
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

    private HomeOfficeSearchService homeOfficeSearchService;

    private HomeOfficeDataErrorsHelper homeOfficeDataErrorsHelper;

    public RequestHomeOfficeDataPreparer(HomeOfficeSearchService homeOfficeSearchService,
                                         HomeOfficeDataErrorsHelper homeOfficeDataErrorsHelper) {

        this.homeOfficeSearchService = homeOfficeSearchService;
        this.homeOfficeDataErrorsHelper = homeOfficeDataErrorsHelper;
    }

    @Override
    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
                && callback.getEvent() == REQUEST_HOME_OFFICE_DATA;
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
        final String homeOfficeReferenceNumber = asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)
                .orElseThrow(() -> new IllegalStateException(
                        "Home office reference for the appeal is not present, caseId: " + caseId));

        final Person.PersonBuilder appellant =
                person()
                        .withGivenName(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                        .withFamilyName(asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""));

        asylumCase.write(APPELLANT_FULL_NAME,
                appellant.build().getGivenName() + " " + appellant.build().getFamilyName());

        HomeOfficeSearchResponse searchResponse;
        DynamicList dynamicList = null;
        try {
            searchResponse = homeOfficeSearchService.getCaseStatus(caseId, homeOfficeReferenceNumber);

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
                    findApplicantsByNameAndDob(searchResponse.getStatus());

            final List<Value> values = new ArrayList<>();
            if (!matchedApplicants.isEmpty()) {

                matchedApplicants.stream().forEach(a -> {
                    values.add(new Value(a.getPerson().getFullName(), a.getPerson().getFullName()));
                });
                values.add(new Value("NoMatch", "No Match"));
                dynamicList = new DynamicList(values.get(0), values);

                asylumCase.write(HOME_OFFICE_SEARCH_STATUS, "SUCCESS");
                asylumCase.write(MATCHING_APPELLANT_DETAILS_FOUND, YesOrNo.YES);
            } else {

                values.add(new Value("NoMatch", "No Match"));

                dynamicList = new DynamicList(values.get(0), values);
                asylumCase.write(MATCHING_APPELLANT_DETAILS_FOUND, YesOrNo.NO);
            }
            asylumCase.write(HOME_OFFICE_APPELLANTS_LIST, dynamicList);


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
        }

        return new PreSubmitCallbackResponse<>(asylumCase);

    }

    List<HomeOfficeCaseStatus> findApplicantsByNameAndDob(List<HomeOfficeCaseStatus> statuses) {

        return statuses.stream()
                .filter(a -> "APPLICANT".equalsIgnoreCase(a.getApplicationStatus().getRoleType().getCode()))
                .collect(Collectors.toList());
    }
}
