package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.presubmit;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPELLANT_DATE_OF_BIRTH;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS_MESSAGE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.Person.PersonBuilder.person;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event.MARK_APPEAL_PAID;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event.PAY_AND_SUBMIT_APPEAL;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event.REQUEST_HOME_OFFICE_DATA;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event.SUBMIT_APPEAL;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ApplicationStatus;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeCaseStatus;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeMetadata;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeSearchResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.Person;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.RejectionReason;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.HomeOfficeSearchService;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.HomeOfficeResponseException;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.util.HomeOfficeDateFormatter;

@Slf4j
@Component
public class AsylumCaseStatusSearchHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private static final String PROBLEM_MESSAGE = "### There is a problem\n\n";

    private static final String HOME_OFFICE_CALL_ERROR_MESSAGE = PROBLEM_MESSAGE
        + "The service has been unable t"
        + "o retrieve the Home Office information about this appeal.\n\n"
        + "[Request the Home Office information](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/requestHomeOfficeData) to"
        + " try again. This may take a few minutes.";

    private static final String HOME_OFFICE_INVALID_REFERENCE_ERROR_MESSAGE = PROBLEM_MESSAGE
        + "The appellant entered the "
        + "Home Office reference number incorrectly. You can contact the appellant to check the reference number"
        + " if you need this information to validate the appeal";

    private static final String HOME_OFFICE_REFERENCE_NOT_FOUND_ERROR_MESSAGE = PROBLEM_MESSAGE
        + "The appellant’s Home Office reference"
        + " number could not be found. You can contact the Home Office to check the reference"
        + " if you need this information to validate the appeal";

    private static final String HOME_OFFICE_APPELLANT_NOT_FOUND_ERROR_MESSAGE = PROBLEM_MESSAGE
        + "The service has been unable to retrieve the Home Office information about this appeal "
        + "because the Home Office reference number does not have any matching appellant data in the system. "
        + "You can contact the Home Office if you need more information to validate the appeal.";

    private static final String HOME_OFFICE_MAIN_APPLICANT_NOT_FOUND_ERROR_MESSAGE = "**Note:** "
        + "The service was unable to retrieve any appellant details from the Home Office because the Home Office data "
        + "does not include a main applicant. You can contact the Home Office if you need this information "
        + "to validate the appeal.";

    private static final DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("yyyy-M-d");

    private static final DateTimeFormatter hoDecisionDateFormatter =
        DateTimeFormatter.ofPattern("yyyy-M-d'T'HH:mm:ss'Z'");

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
            && (callback.getEvent() == SUBMIT_APPEAL
            || callback.getEvent() == PAY_AND_SUBMIT_APPEAL
            || callback.getEvent() == MARK_APPEAL_PAID
            || callback.getEvent() == REQUEST_HOME_OFFICE_DATA);
    }

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
            .orElseThrow(
                () -> new IllegalStateException(
                    "Home office reference for the appeal is not present, caseId: " + caseId
                )
            );

        final String appellantDateOfBirth = asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)
            .orElseThrow(() -> new IllegalStateException("Appellant date of birth is not present."));

        final Person.PersonBuilder appellant =
            person()
                .withGivenName(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .withFamilyName(asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""));

        if (homeOfficeReferenceNumber.length() > 30) {
            log.warn("Home office reference invalid (>30 characters), caseId: {}", caseId);
            asylumCase.write(HOME_OFFICE_SEARCH_STATUS, "FAIL");
            asylumCase.write(
                HOME_OFFICE_SEARCH_STATUS_MESSAGE, HOME_OFFICE_INVALID_REFERENCE_ERROR_MESSAGE);
            return new PreSubmitCallbackResponse<>(asylumCase);
        }

        HomeOfficeSearchResponse searchResponse;
        try {
            searchResponse = homeOfficeSearchService.getCaseStatus(caseId, homeOfficeReferenceNumber);

            if (searchResponse.getErrorDetail() != null) {
                final String errMessage = String.format("Error code: %s, message: %s",
                    searchResponse.getErrorDetail().getErrorCode(),
                    searchResponse.getErrorDetail().getMessageText());
                setErrorMessageForErrorCode(
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

            Optional<HomeOfficeCaseStatus> selectedApplicant =
                selectMainApplicant(caseId, searchResponse.getStatus(), appellant.build(), appellantDateOfBirth);

            if (!selectedApplicant.isPresent()) {
                log.warn("Unable to find MAIN APPLICANT in Home office response, caseId: {}", caseId);
                asylumCase.write(HOME_OFFICE_SEARCH_STATUS, "FAIL");
                asylumCase.write(HOME_OFFICE_SEARCH_STATUS_MESSAGE, HOME_OFFICE_MAIN_APPLICANT_NOT_FOUND_ERROR_MESSAGE);

            } else {

                asylumCase.write(HOME_OFFICE_SEARCH_STATUS, "SUCCESS");
                HomeOfficeCaseStatus selectedMainApplicant = selectedApplicant.get();
                Person person = selectedMainApplicant.getPerson();
                ApplicationStatus applicationStatus = selectedMainApplicant.getApplicationStatus();
                if (isNull(person)) {
                    log.warn(
                        "Note: Unable to find Person details for the applicant in Home office response, caseId: {}",
                        caseId
                    );
                } else {
                    selectedMainApplicant.setDisplayDateOfBirth(
                        HomeOfficeDateFormatter.getPersonDateOfBirth(
                            person.getDayOfBirth(), person.getMonthOfBirth(), person.getYearOfBirth())
                    );
                }

                selectedMainApplicant.setDisplayDecisionDate(
                    HomeOfficeDateFormatter.getIacDateTime(applicationStatus.getDecisionDate()));
                if (applicationStatus.getDecisionCommunication() != null) {
                    selectedMainApplicant.setDisplayDecisionSentDate(
                        HomeOfficeDateFormatter.getIacDateTime(
                            applicationStatus.getDecisionCommunication().getSentDate()
                        )
                    );
                }

                Optional<HomeOfficeMetadata> metadata = selectMetadata(
                    caseId,
                    applicationStatus.getHomeOfficeMetadata()
                );
                if (metadata.isPresent()) {
                    selectedMainApplicant.setDisplayMetadataValueBoolean(
                        ("true".equals(metadata.get().getValueBoolean())) ? "Yes" : "No"
                    );

                    selectedMainApplicant.setDisplayMetadataValueDateTime(
                        HomeOfficeDateFormatter.getIacDateTime(metadata.get().getValueDateTime()));
                }
                selectedMainApplicant.setDisplayRejectionReasons(
                    getRejectionReasonString(applicationStatus.getRejectionReasons()));
                asylumCase.write(AsylumCaseDefinition.HOME_OFFICE_CASE_STATUS_DATA, selectedMainApplicant);

            }
        } catch (HomeOfficeResponseException hoe) {
            setErrorMessageForErrorCode(caseId, asylumCase, hoe.getErrorCode(), hoe.getMessage());
        } catch (Exception e) {
            log.warn(
                "Error while calling Home office case status search: caseId: {}, error message: {}",
                caseId,
                e.getMessage(),
                e)
            ;
            asylumCase.write(HOME_OFFICE_SEARCH_STATUS, "FAIL");
            asylumCase.write(AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS_MESSAGE, HOME_OFFICE_CALL_ERROR_MESSAGE);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    String getRejectionReasonString(List<RejectionReason> rejectionReasons) {
        StringBuilder sb = new StringBuilder("");
        if (rejectionReasons != null && !rejectionReasons.isEmpty()) {
            rejectionReasons.forEach(
                rejectionReason -> sb.append(rejectionReason.getReason()).append("<br />")
            );
            sb.delete(sb.lastIndexOf("<br />"), sb.length());
        }
        return sb.toString();
    }

    Optional<HomeOfficeCaseStatus> selectMainApplicant(long caseId, List<HomeOfficeCaseStatus> statuses,
                                                       Person appellant, String appellantDateOfBirth) {
        Optional<HomeOfficeCaseStatus> searchStatus = Optional.empty();
        if (statuses != null && !statuses.isEmpty()) {
            try {
                searchStatus = statuses.stream()
                    .filter(a -> "APPLICANT".equalsIgnoreCase(a.getApplicationStatus().getRoleType().getCode()))
                    .filter(p -> isApplicantMatched(p, appellant, appellantDateOfBirth))
                    .max(Comparator.comparing(p ->
                        LocalDate.parse(p.getApplicationStatus().getDecisionDate(), hoDecisionDateFormatter)));

            } catch (Exception e) {
                log.warn("Unable to find MAIN APPLICANT in Home office response, caseId: {}", caseId, e);
            }
        }
        return searchStatus;
    }

    Optional<HomeOfficeMetadata> selectMetadata(long caseId, List<HomeOfficeMetadata> metadataList) {
        Optional<HomeOfficeMetadata> metadata = Optional.empty();
        if (metadataList != null && !metadataList.isEmpty()) {
            try {
                metadata = metadataList.stream().filter(
                    metadata1 -> "APPEALABLE".equalsIgnoreCase(metadata1.getCode()))
                    .findFirst();

            } catch (Exception e) {
                log.warn("Unable to find APPEALABLE metadata in Home office response, caseId: {}", caseId, e);
            }
        }
        return metadata;
    }

    boolean isApplicantMatched(HomeOfficeCaseStatus status, Person appellant, String appellantDateOfBirth) {
        
        if (isApplicantNameMatched(status, appellant)
            || isApplicantDobMatched(status, appellantDateOfBirth)) {

            return true;
        }

        return false;
    }

    boolean isApplicantDobMatched(HomeOfficeCaseStatus status, String appellantDateOfBirth) {

        Person person = status.getPerson();

        LocalDate applicantDob =
            LocalDate.parse(person.getYearOfBirth()
                            + "-" + person.getMonthOfBirth()
                            + "-" + person.getDayOfBirth(), dtFormatter);

        return applicantDob.equals(LocalDate.parse(appellantDateOfBirth, dtFormatter));
    }

    boolean isApplicantNameMatched(HomeOfficeCaseStatus status, Person appellant) {

        Person person = status.getPerson();

        if (person.getGivenName() != null && person.getFamilyName() != null) {

            return person.getGivenName().equalsIgnoreCase(appellant.getGivenName())
                   && person.getFamilyName().equalsIgnoreCase(appellant.getFamilyName());
        }

        return false;
    }

    void setErrorMessageForErrorCode(long caseId, AsylumCase asylumCase, String errorCodeStr, String errMessage) {
        final int errorCode = errorCodeStr != null ? Integer.valueOf(errorCodeStr) : 0;
        switch (errorCode) {
            case 1010:
            case 1030:
                asylumCase.write(HOME_OFFICE_SEARCH_STATUS, "FAIL");
                asylumCase.write(
                    AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS_MESSAGE,
                    HOME_OFFICE_APPELLANT_NOT_FOUND_ERROR_MESSAGE);
                log.warn(
                    "Home office response returned with Appellant Not found error, caseId: {}, error message: {}",
                    caseId,
                    errMessage
                );
                break;
            case 1020:
                asylumCase.write(HOME_OFFICE_SEARCH_STATUS, "FAIL");
                asylumCase.write(
                    AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS_MESSAGE,
                    HOME_OFFICE_REFERENCE_NOT_FOUND_ERROR_MESSAGE);
                log.warn(
                    "Home office response returned with HO data Not found error, caseId: {}, error message: {}",
                    caseId,
                    errMessage
                );
                break;
            case 1060:
                asylumCase.write(HOME_OFFICE_SEARCH_STATUS, "FAIL");
                asylumCase.write(
                    AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS_MESSAGE,
                    HOME_OFFICE_INVALID_REFERENCE_ERROR_MESSAGE);
                log.warn(
                    "Home office response returned with HO format error, caseId: {}, error message: {}",
                    caseId,
                    errMessage
                );
                break;
            default:
                asylumCase.write(HOME_OFFICE_SEARCH_STATUS, "FAIL");
                asylumCase.write(
                    AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS_MESSAGE, HOME_OFFICE_CALL_ERROR_MESSAGE);
                log.warn(
                    "Home office response returned with error, caseId: {}, error message: {}",
                    caseId,
                    errMessage
                );
                break;

        }
    }

}
