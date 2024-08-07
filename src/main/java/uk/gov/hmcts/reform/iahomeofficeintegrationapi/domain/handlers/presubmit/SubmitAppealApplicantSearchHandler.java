package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.presubmit;

import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_SEARCH_RESPONSE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS_MESSAGE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event.MARK_APPEAL_PAID;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event.PAY_AND_SUBMIT_APPEAL;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event.SUBMIT_APPEAL;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.HomeOfficeDataErrorsHelper;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.HomeOfficeDataMatchHelper;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ApplicationStatus;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeCaseStatus;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeMetadata;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeSearchResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.Person;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.HomeOfficeSearchService;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.HomeOfficeResponseException;

@Slf4j
@Component
public class SubmitAppealApplicantSearchHandler extends SearchHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private static final String HOME_OFFICE_MULTIPLE_APPELLANTS_ERROR_MESSAGE =
        """
        The Home Office data has returned more than one appellant for this appeal.\s
        ## Do this next
         You need to [request home office data](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/requestHomeOfficeData) to select the correct appellant for this appeal.""";

    private static final String NEXT_STEPS_INFORMATION =
        """

        ## Do this next
        - Contact the Home Office to get the correct details
        - Use [Edit appeal](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/editAppealAfterSubmit) to update the details as required
        - [Request Home Office data](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/requestHomeOfficeData) to match the appellant details with the Home Office details""";

    public SubmitAppealApplicantSearchHandler(HomeOfficeSearchService homeOfficeSearchService,
                                              HomeOfficeDataMatchHelper homeOfficeDataMatchHelper,
                                              HomeOfficeDataErrorsHelper homeOfficeDataErrorsHelper,
                                              FeatureToggler featureToggler) {
        super(homeOfficeSearchService, homeOfficeDataMatchHelper, homeOfficeDataErrorsHelper, featureToggler);
    }

    @Override
    protected List<Event> getEventList() {
        return Arrays.asList(SUBMIT_APPEAL, PAY_AND_SUBMIT_APPEAL, MARK_APPEAL_PAID);
    }

    @Override
    protected boolean checkFeatureToggleKey() {
        return featureToggler.getValue("home-office-uan-feature", false);
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        checkCanHandle(callbackStage, callback);

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        final long caseId = callback.getCaseDetails().getId();
        final String homeOfficeReferenceNumber = getHomeOfficeReferenceNumber(asylumCase, caseId);
        final String appellantDateOfBirth = getAppellantDateOfBirth(asylumCase);
        final Person.PersonBuilder appellant = getAppellant(asylumCase);

        if (homeOfficeReferenceNumber.length() > 30) {
            log.warn(REFERENCE_INVALID_LOG_MESSAGE, caseId);
            updateHomeOfficeStatus(asylumCase, FAIL_STATUS, HOME_OFFICE_INVALID_REFERENCE_ERROR_MESSAGE);
            return new PreSubmitCallbackResponse<>(asylumCase);
        }

        try {
            HomeOfficeSearchResponse searchResponse =
                    homeOfficeSearchService.getCaseStatus(caseId, homeOfficeReferenceNumber);

            if (searchResponse != null) {
                if (RequestHomeOfficeDataPreparer.shouldReturnCallbackResponseWithErrors(asylumCase,
                        caseId, searchResponse, homeOfficeDataErrorsHelper, log)) {
                    return new PreSubmitCallbackResponse<>(asylumCase);
                }

                String homeOfficeSearchResponseJsonStr = new ObjectMapper().writeValueAsString(searchResponse);

                Optional<HomeOfficeCaseStatus> selectedApplicant =
                        selectAnyApplicant(caseId, searchResponse.getStatus());

                if (selectedApplicant.isEmpty()) {
                    log.warn(NO_APPLICANT_FOUND_ERROR_LOG_MESSAGE, caseId);
                    updateHomeOfficeStatus(asylumCase, FAIL_STATUS, HOME_OFFICE_MAIN_APPLICANT_NOT_FOUND_ERROR_MESSAGE
                            + NEXT_STEPS_INFORMATION);
                } else {
                    processApplicantSearchResponse(caseId, searchResponse, appellant, appellantDateOfBirth, asylumCase, homeOfficeSearchResponseJsonStr);
                }
            } else {
                log.warn(
                        "Home office search response is null for the caseId: {}",
                        caseId);
                updateHomeOfficeStatus(asylumCase, FAIL_STATUS, HOME_OFFICE_CALL_ERROR_MESSAGE);
            }
        } catch (HomeOfficeResponseException hoe) {
            homeOfficeDataErrorsHelper
                    .setErrorMessageForErrorCode(caseId, asylumCase, hoe.getErrorCode(), hoe.getMessage());
        } catch (Exception e) {
            log.warn(
                STATUS_SEARCH_ERROR_MESSAGE,
                caseId,
                e.getMessage(),
                e);
            updateHomeOfficeStatus(asylumCase, FAIL_STATUS, HOME_OFFICE_CALL_ERROR_MESSAGE);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void processApplicantSearchResponse(long caseId,
                                                HomeOfficeSearchResponse searchResponse,
                                                Person.PersonBuilder appellant,
                                                String appellantDateOfBirth,
                                                AsylumCase asylumCase,
                                                String homeOfficeSearchResponseJsonStr) {
        List<HomeOfficeCaseStatus> matchedApplicants =
                selectMainApplicant(caseId,
                        searchResponse.getStatus(),
                        appellant.build(),
                        appellantDateOfBirth);
        if (matchedApplicants.isEmpty()) {
            log.warn(MAIN_APPLICANT_NOT_FOUND_ERROR_LOG_MESSAGE, caseId);
            updateHomeOfficeStatus(asylumCase, FAIL_STATUS, HOME_OFFICE_WRONG_APPLICANT_NOT_FOUND_ERROR_MESSAGE
                    + NEXT_STEPS_INFORMATION);
        } else if (matchedApplicants.size() > 1) {
            log.warn("More than one MAIN APPLICANT found in Home office response, caseId: {}", caseId);
            asylumCase.write(HOME_OFFICE_SEARCH_RESPONSE, homeOfficeSearchResponseJsonStr);
            updateHomeOfficeStatus(asylumCase, "MULTIPLE", HOME_OFFICE_MULTIPLE_APPELLANTS_ERROR_MESSAGE);
        } else {
            asylumCase.write(HOME_OFFICE_SEARCH_RESPONSE, homeOfficeSearchResponseJsonStr);
            asylumCase.write(HOME_OFFICE_SEARCH_STATUS, SUCCESS_STATUS);

            matchedApplicants.stream().forEach(a -> {
                Person person = a.getPerson();
                ApplicationStatus applicationStatus = a.getApplicationStatus();
                handlePersonDetails(a, person, caseId);

                setDisplayDecisionDate(a, applicationStatus);
                setDecisionSentDateIfPresent(applicationStatus, a);

                Optional<HomeOfficeMetadata> metadata = selectMetadata(
                        caseId,
                        applicationStatus.getHomeOfficeMetadata()
                );
                handleMetadata(a, metadata);
                setRejectionReasons(a, applicationStatus.getRejectionReasons());
                asylumCase.write(AsylumCaseDefinition.HOME_OFFICE_CASE_STATUS_DATA, a);
            });
        }
    }

    List<HomeOfficeCaseStatus> selectMainApplicant(long caseId, List<HomeOfficeCaseStatus> statuses,
                                                   Person appellant, String appellantDateOfBirth) {
        if (statuses != null && !statuses.isEmpty()) {
            try {
                return statuses.stream()
                        .filter(p -> homeOfficeDataMatchHelper.isApplicantMatched(p, appellant, appellantDateOfBirth))
                        .toList();

            } catch (Exception e) {
                log.warn(MAIN_APPLICANT_NOT_FOUND_ERROR_LOG_MESSAGE, caseId, e);
            }
        }
        return Collections.emptyList();
    }

    private static void updateHomeOfficeStatus(AsylumCase asylumCase,
                                               String status,
                                               String statusMessage) {
        asylumCase.write(HOME_OFFICE_SEARCH_STATUS, status);
        asylumCase.write(HOME_OFFICE_SEARCH_STATUS_MESSAGE, statusMessage);
    }
}
