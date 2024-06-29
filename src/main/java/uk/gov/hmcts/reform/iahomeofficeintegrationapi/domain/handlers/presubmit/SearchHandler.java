package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.presubmit;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.HomeOfficeDataErrorsHelper;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.HomeOfficeDataMatchHelper;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ApplicationStatus;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeCaseStatus;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeMetadata;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.Person;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.RejectionReason;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.HomeOfficeSearchService;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.util.HomeOfficeDateFormatter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPELLANT_DATE_OF_BIRTH;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.Person.PersonBuilder.person;

@Slf4j
public abstract class SearchHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private static final DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("yyyy-M-d");
    protected HomeOfficeSearchService homeOfficeSearchService;
    protected HomeOfficeDataMatchHelper homeOfficeDataMatchHelper;
    protected HomeOfficeDataErrorsHelper homeOfficeDataErrorsHelper;
    protected FeatureToggler featureToggler;

    public SearchHandler(HomeOfficeSearchService homeOfficeSearchService,
                         HomeOfficeDataMatchHelper homeOfficeDataMatchHelper,
                         HomeOfficeDataErrorsHelper homeOfficeDataErrorsHelper,
                         FeatureToggler featureToggler) {
        this.homeOfficeSearchService = homeOfficeSearchService;
        this.homeOfficeDataMatchHelper = homeOfficeDataMatchHelper;
        this.homeOfficeDataErrorsHelper = homeOfficeDataErrorsHelper;
        this.featureToggler = featureToggler;
    }

    protected static final String FAIL_STATUS = "FAIL";
    protected static final String SUCCESS_STATUS = "SUCCESS";
    protected static final String REFERENCE_INVALID_LOG_MESSAGE = "Home office reference invalid (>30 characters), caseId: {}";
    protected static final String STATUS_SEARCH_ERROR_MESSAGE = "Error while calling Home office case status search: caseId: {}, error message: {}";
    protected static final String MAIN_APPLICANT_NOT_FOUND_ERROR_LOG_MESSAGE = "Unable to find MAIN APPLICANT in Home office response, caseId: {}";
    protected static final String NO_APPLICANT_FOUND_ERROR_LOG_MESSAGE = "Unable to find Any APPLICANT in Home office response, caseId: {}";
    protected static final String HOME_OFFICE_CALL_ERROR_MESSAGE =
            """
            ### There is a problem
            
            The service has been unable to retrieve the Home Office information about this appeal.
            
            [Request the Home Office information](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/requestHomeOfficeData) to try again. This may take a few minutes.""";

    protected static final String HOME_OFFICE_INVALID_REFERENCE_ERROR_MESSAGE =
            """
            ### There is a problem

            The appellant entered the Home Office reference number incorrectly. You can contact the appellant to check\
             the reference number if you need this information to validate the appeal""";

    protected static final String HOME_OFFICE_MAIN_APPLICANT_NOT_FOUND_ERROR_MESSAGE =
            """
            **Note:** The service was unable to retrieve any appellant details from the Home Office because the Home\
             Office data does not include a main applicant. You can contact the Home Office if you need this\
             information to validate the appeal.""";

    protected static final String HOME_OFFICE_WRONG_APPLICANT_NOT_FOUND_ERROR_MESSAGE =
            """
            **Note:** The service has been unable to retrieve the Home Office information about this appeal because\
             the Home Office Reference/Case ID, date of birth or name submitted by the appellant do not match the\
             details stored by the Home Office""";

    protected String getHomeOfficeReferenceNumber(AsylumCase asylumCase, long caseId) {
        return asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Home office reference for the appeal is not present, caseId: " + caseId));
    }

    protected String getAppellantDateOfBirth(AsylumCase asylumCase) {
        return asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)
                .orElseThrow(() -> new IllegalStateException("Appellant date of birth is not present."));
    }

    protected Person.PersonBuilder getAppellant(AsylumCase asylumCase) {
        return person()
                .withGivenName(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .withFamilyName(asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""));
    }

    protected Optional<HomeOfficeMetadata> selectMetadata(long caseId,
                                                          List<HomeOfficeMetadata> metadataList) {
        if (metadataList != null && !metadataList.isEmpty()) {
            try {
                return metadataList.stream().filter(metadata1 ->
                        "APPEALABLE".equalsIgnoreCase(metadata1.getCode())).findFirst();

            } catch (Exception e) {
                log.warn("Unable to find APPEALABLE metadata in Home office response, caseId: {}", caseId, e);
            }
        }
        return Optional.empty();
    }

    protected String getRejectionReasonString(List<RejectionReason> rejectionReasons) {
        StringBuilder sb = new StringBuilder();
        if (rejectionReasons != null && !rejectionReasons.isEmpty()) {
            rejectionReasons.forEach(
                    rejectionReason -> sb.append(rejectionReason.getReason()).append("<br />")
            );
            sb.delete(sb.lastIndexOf("<br />"), sb.length());
        }
        return sb.toString();
    }

    protected LocalDate getFormattedDecisionDate(String decisionDate) {
        boolean dateWithTime = decisionDate.contains("T");
        return dateWithTime
                ? LocalDate.parse(decisionDate.split("T")[0], dtFormatter)
                : LocalDate.parse(decisionDate, dtFormatter);
    }

    protected Optional<HomeOfficeCaseStatus> selectAnyApplicant(long caseId, List<HomeOfficeCaseStatus> statuses) {
        Optional<HomeOfficeCaseStatus> searchStatus = Optional.empty();
        if (statuses != null && !statuses.isEmpty()) {
            try {
                searchStatus = statuses.stream()
                        .filter(a -> "APPLICANT".equalsIgnoreCase(a.getApplicationStatus().getRoleType().getCode()))
                        .findAny();

            } catch (Exception e) {
                log.warn("Unable to find APPLICANT in Home office response, caseId: {}", caseId, e);
            }
        }
        return searchStatus;
    }

    protected void checkCanHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }
    }

    protected static void setDisplayDateOfBirth(HomeOfficeCaseStatus selectedApplicant, Person person) {
        selectedApplicant.setDisplayDateOfBirth(
                HomeOfficeDateFormatter.getPersonDateOfBirth(person.getDayOfBirth(),
                        person.getMonthOfBirth(), person.getYearOfBirth()));
    }

    protected void setMetadataValueBoolean(HomeOfficeCaseStatus selectedApplicant, String valueBoolean) {
        selectedApplicant.setDisplayMetadataValueBoolean(
                ("true".equals(valueBoolean)) ? "Yes" : "No"
        );
    }

    protected void setMetadataTimeValue(HomeOfficeCaseStatus selectedApplicant, String time) {
        selectedApplicant.setDisplayMetadataValueDateTime(
                HomeOfficeDateFormatter.getIacDateTime(time));
    }

    protected void setDisplayDecisionSentDate(HomeOfficeCaseStatus a, ApplicationStatus applicationStatus) {
        a.setDisplayDecisionSentDate(
                HomeOfficeDateFormatter.getIacDateTime(
                        applicationStatus.getDecisionCommunication().getSentDate()
                )
        );
    }

    protected void setDecisionSentDateIfPresent(ApplicationStatus applicationStatus,HomeOfficeCaseStatus selectedMainApplicant) {
        if (applicationStatus.getDecisionCommunication() != null) {
            setDisplayDecisionSentDate(selectedMainApplicant, applicationStatus);
        }
    }

    protected void setRejectionReasons(HomeOfficeCaseStatus a, List<RejectionReason> rejectionReasons) {
        a.setDisplayRejectionReasons(
                getRejectionReasonString(rejectionReasons));
    }

    protected void handlePersonDetails(HomeOfficeCaseStatus a, Person person, long caseId) {
        if (isNull(person)) {
            log.warn(
                    "Note: Unable to find Person details "
                            + "for the applicant in Home office response, caseId: {}",
                    caseId
            );
        } else {
            setDisplayDateOfBirth(a, person);
        }
    }

    protected void handleMetadata(HomeOfficeCaseStatus selectedMainApplicant,
                                  Optional<HomeOfficeMetadata> metadata) {
        if (metadata.isPresent()) {
            setMetadataValueBoolean(selectedMainApplicant, metadata.get().getValueBoolean());
            setMetadataTimeValue(selectedMainApplicant, metadata.get().getValueDateTime());
        }
    }

    protected void setDisplayDecisionDate(HomeOfficeCaseStatus a, ApplicationStatus applicationStatus) {
        a.setDisplayDecisionDate(
                HomeOfficeDateFormatter.getIacDateTime(applicationStatus.getDecisionDate()));
    }

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && getEventList().contains(callback.getEvent())
                && checkFeatureToggleKey();
    }

    protected abstract List<Event> getEventList();

    protected abstract boolean checkFeatureToggleKey();
}