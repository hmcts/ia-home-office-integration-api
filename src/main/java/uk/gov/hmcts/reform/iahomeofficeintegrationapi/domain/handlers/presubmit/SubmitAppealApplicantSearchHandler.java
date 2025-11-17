package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.presubmit;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPELLANT_DATE_OF_BIRTH;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_SEARCH_RESPONSE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS_MESSAGE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.Person.PersonBuilder.person;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event.MARK_APPEAL_PAID;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event.PAY_AND_SUBMIT_APPEAL;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event.SUBMIT_APPEAL;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.RejectionReason;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.HomeOfficeSearchService;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.HomeOfficeResponseException;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.util.HomeOfficeDateFormatter;

@Slf4j
@Component
public class SubmitAppealApplicantSearchHandler implements PreSubmitCallbackHandler<AsylumCase> {

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

    private static final String HOME_OFFICE_MAIN_APPLICANT_NOT_FOUND_ERROR_MESSAGE = "**Note:** "
        + "The service was unable to retrieve any appellant details from the Home Office because the Home Office data "
        + "does not include a main applicant. You can contact the Home Office if you need this information "
        + "to validate the appeal.\n## Do this next"
        + "\r\n- Contact the Home Office to get the correct details"
        + "\r\n- Use [Edit appeal](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/editAppealAfterSubmit) to update "
        + "the details as required\r\n- [Request Home Office data](/case/IA/Asylum/${[CASE_REFERENCE]}"
        + "/trigger/requestHomeOfficeData) to match the appellant details with the Home Office details";

    private static final String HOME_OFFICE_WRONG_APPLICANT_NOT_FOUND_ERROR_MESSAGE = "**Note:** "
            + "The service has been unable to retrieve the Home Office information about this appeal "
            + "because the Home Office Reference/Case ID, data of birth or name submitted by the appellant do not "
            + "match the details stored by the Home Office\n## Do this next"
            + "\r\n- Contact the Home Office to get the correct details"
            + "\r\n- Use [Edit appeal](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/editAppealAfterSubmit) to update "
            + "the details as required"
            + "\r\n- [Request Home Office data](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/requestHomeOfficeData) "
            + "to match the appellant details with the Home Office details";

    private static final String HOME_OFFICE_MULTIPLE_APPELLANTS_ERROR_MESSAGE = "The Home Office data has returned "
            + "more than one appellant for this appeal. "
            + "\r\n## Do this next"
            + "\r\n You need to [request home office data](/case/IA/Asylum/"
            + "${[CASE_REFERENCE]}/trigger/requestHomeOfficeData) to select the correct appellant for this appeal.";

    private static final DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("yyyy-M-d");

    private HomeOfficeSearchService homeOfficeSearchService;

    private HomeOfficeDataErrorsHelper homeOfficeDataErrorsHelper;

    private HomeOfficeDataMatchHelper homeOfficeDataMatchHelper;

    private final FeatureToggler featureToggler;

    public SubmitAppealApplicantSearchHandler(HomeOfficeSearchService homeOfficeSearchService,
                                              HomeOfficeDataErrorsHelper homeOfficeDataErrorsHelper,
                                              HomeOfficeDataMatchHelper homeOfficeDataMatchHelper,
                                              FeatureToggler featureToggler) {
        this.homeOfficeSearchService = homeOfficeSearchService;
        this.homeOfficeDataErrorsHelper = homeOfficeDataErrorsHelper;
        this.homeOfficeDataMatchHelper = homeOfficeDataMatchHelper;
        this.featureToggler = featureToggler;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && Arrays.asList(SUBMIT_APPEAL, PAY_AND_SUBMIT_APPEAL, MARK_APPEAL_PAID).contains(callback.getEvent())
                && featureToggler.getValue("home-office-uan-feature", false);
    }

    private void updateAsylumCase(AsylumCase asylumCase,
                                        long caseId,
                                        HomeOfficeSearchResponse searchResponse,
                                        Person.PersonBuilder appellant,
                                        String appellantDateOfBirth,
                                        String homeOfficeSearchResponseJsonStr) {

        Optional<HomeOfficeCaseStatus> selectedApplicant =
                selectAnyApplicant(caseId, searchResponse.getStatus());

        if (!selectedApplicant.isPresent()) {
            log.warn("Unable to find Any APPLICANT in Home office response, caseId: {}", caseId);
            asylumCase.write(HOME_OFFICE_SEARCH_STATUS, "FAIL");
            asylumCase.write(HOME_OFFICE_SEARCH_STATUS_MESSAGE,
                    HOME_OFFICE_MAIN_APPLICANT_NOT_FOUND_ERROR_MESSAGE);

        } else {
            List<HomeOfficeCaseStatus> matchedApplicants =
                    selectMainApplicant(caseId,
                            searchResponse.getStatus(),
                            appellant.build(),
                            appellantDateOfBirth);

            if (matchedApplicants.isEmpty() || isNull(matchedApplicants)) {

                log.warn("Unable to find MAIN APPLICANT in Home office response, caseId: {}", caseId);
                asylumCase.write(HOME_OFFICE_SEARCH_STATUS, "FAIL");
                asylumCase.write(HOME_OFFICE_SEARCH_STATUS_MESSAGE,
                        HOME_OFFICE_WRONG_APPLICANT_NOT_FOUND_ERROR_MESSAGE);
            } else if (matchedApplicants.size() > 1) {

                log.warn("More than one MAIN APPLICANT found in Home office response, caseId: {}", caseId);
                asylumCase.write(HOME_OFFICE_SEARCH_RESPONSE, homeOfficeSearchResponseJsonStr);
                asylumCase.write(HOME_OFFICE_SEARCH_STATUS, "MULTIPLE");
                asylumCase.write(HOME_OFFICE_SEARCH_STATUS_MESSAGE,
                        HOME_OFFICE_MULTIPLE_APPELLANTS_ERROR_MESSAGE);
            } else {
                asylumCase.write(HOME_OFFICE_SEARCH_RESPONSE, homeOfficeSearchResponseJsonStr);
                asylumCase.write(HOME_OFFICE_SEARCH_STATUS, "SUCCESS");

                matchedApplicants.stream().forEach(a -> {

                    Person person = a.getPerson();
                    ApplicationStatus applicationStatus = a.getApplicationStatus();
                    if (isNull(person)) {
                        log.warn(
                                "Note: Unable to find Person details "
                                        + "for the applicant in Home office response, caseId: {}",
                                caseId
                        );
                    } else {
                        a.setDisplayDateOfBirth(
                                HomeOfficeDateFormatter.getPersonDateOfBirth(person.getDayOfBirth(),
                                        person.getMonthOfBirth(), person.getYearOfBirth()));
                    }

                    a.setDisplayDecisionDate(
                            HomeOfficeDateFormatter.getIacDateTime(applicationStatus.getDecisionDate()));
                    if (applicationStatus.getDecisionCommunication() != null) {
                        a.setDisplayDecisionSentDate(
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
                        a.setDisplayMetadataValueBoolean(
                                ("true".equals(metadata.get().getValueBoolean())) ? "Yes" : "No"
                        );

                        a.setDisplayMetadataValueDateTime(
                                HomeOfficeDateFormatter.getIacDateTime(metadata.get().getValueDateTime()));
                    }
                    a.setDisplayRejectionReasons(
                            getRejectionReasonString(applicationStatus.getRejectionReasons()));
                    asylumCase.write(AsylumCaseDefinition.HOME_OFFICE_CASE_STATUS_DATA, a);
                });
            }

        }
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

        try {
            HomeOfficeSearchResponse searchResponse =
                    homeOfficeSearchService.getCaseStatus(caseId, homeOfficeReferenceNumber);

            if (searchResponse != null) {
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

                String homeOfficeSearchResponseJsonStr = new ObjectMapper().writeValueAsString(searchResponse);

                updateAsylumCase(asylumCase,
                        caseId,
                        searchResponse,
                        appellant,
                        appellantDateOfBirth,
                        homeOfficeSearchResponseJsonStr);
            } else {

                log.warn(
                        "Home office search response is null for the caseId: {}",
                        caseId);
                asylumCase.write(HOME_OFFICE_SEARCH_STATUS, "FAIL");
                asylumCase.write(AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS_MESSAGE,
                        HOME_OFFICE_CALL_ERROR_MESSAGE);
            }
        } catch (HomeOfficeResponseException hoe) {
            homeOfficeDataErrorsHelper
                    .setErrorMessageForErrorCode(caseId, asylumCase, hoe.getErrorCode(), hoe.getMessage());
        } catch (Exception e) {
            log.warn(
                "Error while calling Home office case status search: caseId: {}, error message: {}",
                caseId,
                e.getMessage(),
                e);
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

    List<HomeOfficeCaseStatus> selectMainApplicant(long caseId, List<HomeOfficeCaseStatus> statuses,
                                                       Person appellant, String appellantDateOfBirth) {
        if (statuses != null && !statuses.isEmpty()) {
            try {
                return statuses.stream()
                    .filter(p -> homeOfficeDataMatchHelper.isApplicantMatched(p, appellant, appellantDateOfBirth))
                    .collect(Collectors.toList());

            } catch (Exception e) {
                log.warn("Unable to find MAIN APPLICANT in Home office response, caseId: {}", caseId, e);
            }
        }
        return null;
    }

    LocalDate getFormattedDecisionDate(String decisionDate) {
        boolean dateWithTime = decisionDate.contains("T");
        return dateWithTime
                ? LocalDate.parse(decisionDate.split("T")[0], dtFormatter)
                : LocalDate.parse(decisionDate, dtFormatter);
    }

    Optional<HomeOfficeCaseStatus> selectAnyApplicant(long caseId, List<HomeOfficeCaseStatus> statuses) {
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
}
