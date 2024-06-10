package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.presubmit;

import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS_MESSAGE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event.MARK_APPEAL_PAID;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event.PAY_AND_SUBMIT_APPEAL;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event.REQUEST_HOME_OFFICE_DATA;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event.SUBMIT_APPEAL;

import java.util.Arrays;
import java.util.Comparator;
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
public class AsylumCaseStatusSearchHandler extends SearchHandler implements PreSubmitCallbackHandler<AsylumCase> {

    public AsylumCaseStatusSearchHandler(HomeOfficeSearchService homeOfficeSearchService,
                                         HomeOfficeDataMatchHelper homeOfficeDataMatchHelper,
                                         HomeOfficeDataErrorsHelper homeOfficeDataErrorsHelper,
                                         FeatureToggler featureToggler) {
        super(homeOfficeSearchService, homeOfficeDataMatchHelper, homeOfficeDataErrorsHelper, featureToggler);
    }

    @Override
    protected List<Event> getEventList() {
        return Arrays.asList(SUBMIT_APPEAL, PAY_AND_SUBMIT_APPEAL, MARK_APPEAL_PAID, REQUEST_HOME_OFFICE_DATA);
    }

    @Override
    protected boolean checkFeatureToggleKey() {
        return !featureToggler.getValue("home-office-uan-feature", false);
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
        final Person.PersonBuilder appellant =
                getAppellant(asylumCase);

        if (homeOfficeReferenceNumber.length() > 30) {
            updateAsylumCaseStatusForInvalidHomeOfficeData(REFERENCE_INVALID_LOG_MESSAGE,
                caseId, asylumCase, HOME_OFFICE_INVALID_REFERENCE_ERROR_MESSAGE);
            return new PreSubmitCallbackResponse<>(asylumCase);
        }

        HomeOfficeSearchResponse searchResponse;
        try {
            searchResponse = homeOfficeSearchService.getCaseStatus(caseId, homeOfficeReferenceNumber);

            if (searchResponse.getErrorDetail() != null) {
                updateAsylumCaseForGeneralHomeOfficeError(searchResponse, caseId, asylumCase);
                return new PreSubmitCallbackResponse<>(asylumCase);
            }

            Optional<HomeOfficeCaseStatus> selectedApplicant =
                    selectAnyApplicant(caseId, searchResponse.getStatus());

            if (selectedApplicant.isEmpty()) {
                updateAsylumCaseStatusForInvalidHomeOfficeData(NO_APPLICANT_FOUND_ERROR_LOG_MESSAGE,
                        caseId, asylumCase, HOME_OFFICE_MAIN_APPLICANT_NOT_FOUND_ERROR_MESSAGE);

            } else {
                selectedApplicant =
                        selectMainApplicant(
                                caseId,
                                searchResponse.getStatus(),
                                appellant.build(),
                                appellantDateOfBirth
                        );
                processSelectedApplicant(selectedApplicant, caseId, asylumCase);
            }
        } catch (HomeOfficeResponseException hoe) {
            homeOfficeDataErrorsHelper.setErrorMessageForErrorCode(caseId, asylumCase, hoe.getErrorCode(), hoe.getMessage());
        } catch (Exception e) {
            log.warn(
                    STATUS_SEARCH_ERROR_MESSAGE,
                    caseId,
                    e.getMessage(),
                    e)
            ;
            asylumCase.write(HOME_OFFICE_SEARCH_STATUS, FAIL_STATUS);
            asylumCase.write(AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS_MESSAGE, HOME_OFFICE_CALL_ERROR_MESSAGE);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void processSelectedApplicant(Optional<HomeOfficeCaseStatus> selectedApplicant,
                                          long caseId,
                                          AsylumCase asylumCase) {
        if (selectedApplicant.isEmpty()) {
            handleNoMainApplicantFound(caseId, asylumCase);
        } else {
            handleMainApplicantFound(selectedApplicant.get(), caseId, asylumCase);
        }
    }

    private void handleNoMainApplicantFound(long caseId, AsylumCase asylumCase) {
        updateAsylumCaseStatusForInvalidHomeOfficeData(MAIN_APPLICANT_NOT_FOUND_ERROR_LOG_MESSAGE,
                caseId, asylumCase, HOME_OFFICE_WRONG_APPLICANT_NOT_FOUND_ERROR_MESSAGE);
    }

    private void handleMainApplicantFound(HomeOfficeCaseStatus selectedMainApplicant,
                                          long caseId,
                                          AsylumCase asylumCase) {
        asylumCase.write(HOME_OFFICE_SEARCH_STATUS, SUCCESS_STATUS);
        Person person = selectedMainApplicant.getPerson();
        ApplicationStatus applicationStatus = selectedMainApplicant.getApplicationStatus();
        handlePersonDetails(selectedMainApplicant, person, caseId);
        handleApplicationStatus(applicationStatus, selectedMainApplicant, caseId, asylumCase);
    }

    private void handleApplicationStatus(ApplicationStatus applicationStatus,
                                         HomeOfficeCaseStatus selectedMainApplicant,
                                         long caseId,
                                         AsylumCase asylumCase) {
        setDisplayDecisionDate(selectedMainApplicant, applicationStatus);
        setDecisionSentDateIfPresent(applicationStatus, selectedMainApplicant);
        Optional<HomeOfficeMetadata> metadata = selectMetadata(caseId, applicationStatus.getHomeOfficeMetadata());
        handleMetadata(selectedMainApplicant, metadata);
        setRejectionReasons(selectedMainApplicant, applicationStatus.getRejectionReasons());
        asylumCase.write(AsylumCaseDefinition.HOME_OFFICE_CASE_STATUS_DATA, selectedMainApplicant);
    }

    private void updateAsylumCaseForGeneralHomeOfficeError(HomeOfficeSearchResponse searchResponse,
                                                           long caseId,
                                                           AsylumCase asylumCase) {
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
    }

    private static void updateAsylumCaseStatusForInvalidHomeOfficeData(String format,
                                                                       long caseId,
                                                                       AsylumCase asylumCase,
                                                                       String homeOfficeInvalidReferenceErrorMessage) {
        log.warn(format, caseId);
        asylumCase.write(HOME_OFFICE_SEARCH_STATUS, FAIL_STATUS);
        asylumCase.write(
                HOME_OFFICE_SEARCH_STATUS_MESSAGE, homeOfficeInvalidReferenceErrorMessage);
    }

    Optional<HomeOfficeCaseStatus> selectMainApplicant(long caseId,
                                                       List<HomeOfficeCaseStatus> statuses,
                                                       Person appellant,
                                                       String appellantDateOfBirth) {
        Optional<HomeOfficeCaseStatus> searchStatus = Optional.empty();
        if (statuses != null && !statuses.isEmpty()) {
            try {
                searchStatus = statuses.stream()
                        .filter(a -> "APPLICANT".equalsIgnoreCase(a.getApplicationStatus().getRoleType().getCode()))
                        .filter(p -> homeOfficeDataMatchHelper.isApplicantMatched(p, appellant, appellantDateOfBirth))
                        .max(Comparator.comparing(p ->
                                getFormattedDecisionDate(p.getApplicationStatus().getDecisionDate())));

            } catch (Exception e) {
                log.warn(MAIN_APPLICANT_NOT_FOUND_ERROR_LOG_MESSAGE, caseId, e);
            }
        }
        return searchStatus;
    }

}