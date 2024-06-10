package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_APPELLANTS_LIST;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_CASE_STATUS_DATA;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER_BEFORE_EDIT;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_SEARCH_NO_MATCH;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_SEARCH_RESPONSE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS_MESSAGE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event.REQUEST_HOME_OFFICE_DATA;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ApplicationStatus;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.CodeWithDescription;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeCaseStatus;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeMetadata;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeSearchResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.Person;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.RejectionReason;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.DynamicList;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.util.HomeOfficeDateFormatter;

@Slf4j
@Component
public class RequestHomeOfficeDataHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private static final String PROBLEM_MESSAGE = "### There is a problem\n\n";

    private static final String HOME_OFFICE_CALL_ERROR_MESSAGE = PROBLEM_MESSAGE
            + "The service has been unable t"
            + "o retrieve the Home Office information about this appeal.\n\n"
            + "[Request the Home Office information](/case/IA/Asylum/${[CASE_REFERENCE]}/"
            + "trigger/requestHomeOfficeData) to try again. This may take a few minutes.";

    private final FeatureToggler featureToggler;

    public RequestHomeOfficeDataHandler(FeatureToggler featureToggler) {
        this.featureToggler = featureToggler;
    }

    @Override
    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
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

        String selectedApplicant = asylumCase.read(HOME_OFFICE_APPELLANTS_LIST, DynamicList.class)
                .orElseThrow(() -> new IllegalStateException("Appellant not selected form the list."))
                .getValue().getLabel();

        HomeOfficeSearchResponse searchResponse;

        try {

            if (selectedApplicant.equalsIgnoreCase("No Match")) {

                String noMatch = "No match";
                Person noMatchingPerson = Person.PersonBuilder.person()
                        .withGivenName(noMatch)
                        .withFamilyName(noMatch)
                        .withFullName(noMatch)
                        .withNationality(new CodeWithDescription(noMatch, noMatch))
                        .withGender(new CodeWithDescription(noMatch, noMatch))
                        .build();

                CodeWithDescription noMatchCodeWithDesc = new CodeWithDescription(noMatch, noMatch);
                ApplicationStatus noMatchApplicationStatus =
                        new ApplicationStatus.Builder().build();

                HomeOfficeCaseStatus noMatchApplicant =
                        new HomeOfficeCaseStatus(noMatchingPerson, noMatchApplicationStatus, null,
                                null, null, null,
                                null, null, null,
                                null);
                noMatchApplicant.setDisplayDateOfBirth(noMatch);

                asylumCase.write(HOME_OFFICE_SEARCH_STATUS, "SUCCESS");
                asylumCase.write(HOME_OFFICE_SEARCH_NO_MATCH, "NO_MATCH");
                asylumCase.write(HOME_OFFICE_CASE_STATUS_DATA, noMatchApplicant);

            } else {
                String homeOfficeSearchResponseJsonStr =
                        asylumCase.read(HOME_OFFICE_SEARCH_RESPONSE, String.class)
                                .orElseThrow(() -> new IllegalStateException("Home search response is not present."));

                searchResponse = new ObjectMapper()
                        .readValue(homeOfficeSearchResponseJsonStr, HomeOfficeSearchResponse.class);

                String selectedApplicantName = selectedApplicant.split("-")[0];
                String selectedApplicantDob = selectedApplicant.split("-")[1];

                Optional<HomeOfficeCaseStatus> optMatchedApplicant =
                        findApplicantByNameAndDob(
                                searchResponse.getStatus(), selectedApplicantName, selectedApplicantDob);

                if (!optMatchedApplicant.isEmpty()) {

                    HomeOfficeCaseStatus matchedApplicant = optMatchedApplicant.get();
                    Person person = matchedApplicant.getPerson();
                    ApplicationStatus applicationStatus = matchedApplicant.getApplicationStatus();
                    Optional<HomeOfficeMetadata> metadata =
                            selectMetadata(caseId, applicationStatus.getHomeOfficeMetadata());

                    matchedApplicant
                            .setDisplayDateOfBirth(HomeOfficeDateFormatter.getPersonDateOfBirth(person.getDayOfBirth(),
                            person.getMonthOfBirth(), person.getYearOfBirth()));
                    matchedApplicant.setDisplayDecisionDate(
                            HomeOfficeDateFormatter.getIacDateTime(applicationStatus.getDecisionDate()));

                    if (metadata.isPresent()) {
                        matchedApplicant.setDisplayMetadataValueBoolean(
                                ("true".equals(metadata.get().getValueBoolean())) ? "Yes" : "No"
                        );

                        matchedApplicant.setDisplayMetadataValueDateTime(
                                HomeOfficeDateFormatter.getIacDateTime(metadata.get().getValueDateTime()));
                    }
                    matchedApplicant.setDisplayRejectionReasons(
                            getRejectionReasonString(applicationStatus.getRejectionReasons()));

                    asylumCase.write(HOME_OFFICE_SEARCH_STATUS, "SUCCESS");
                    asylumCase.write(HOME_OFFICE_CASE_STATUS_DATA, matchedApplicant);

                    asylumCase.clear(HOME_OFFICE_SEARCH_NO_MATCH);
                }
            }
        } catch (Exception e) {
            log.warn(
                    "Error processing Home Office response from case data: {}",
                    caseId,
                    e.getMessage(),
                    e);
            asylumCase.write(HOME_OFFICE_SEARCH_STATUS, "FAIL");
            asylumCase.write(HOME_OFFICE_SEARCH_STATUS_MESSAGE, HOME_OFFICE_CALL_ERROR_MESSAGE);
        }

        asylumCase.clear(HOME_OFFICE_SEARCH_RESPONSE);
        asylumCase.clear(HOME_OFFICE_REFERENCE_NUMBER_BEFORE_EDIT);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    Optional<HomeOfficeCaseStatus> findApplicantByNameAndDob(
            List<HomeOfficeCaseStatus> statuses,
            String selectedApplicantName,
            String selectedApplicantDob) {

        return statuses.stream()
                .filter(p -> isApplicantMatched(p, selectedApplicantName, selectedApplicantDob))
                .findAny();
    }

    Optional<HomeOfficeMetadata> selectMetadata(long caseId, List<HomeOfficeMetadata> metadataList) {
        Optional<HomeOfficeMetadata> metadata = Optional.empty();
        if (metadataList != null && !metadataList.isEmpty()) {
            try {
                metadata =
                        metadataList.stream()
                                .filter(metadata1 ->
                                        "APPEALABLE".equalsIgnoreCase(metadata1.getCode()))
                                .findFirst();

            } catch (Exception e) {
                log.warn("Unable to find APPEALABLE metadata in Home office response, caseId: {}", caseId, e);
            }
        }
        return metadata;
    }

    boolean isApplicantMatched(HomeOfficeCaseStatus status, String selectedApplicantName, String appellantDateOfBirth) {
        return isApplicantNameMatched(status, selectedApplicantName) && isApplicantDobMatched(status, appellantDateOfBirth);
    }

    boolean isApplicantNameMatched(HomeOfficeCaseStatus status, String selectedApplicantName) {

        Person person = status.getPerson();

        if (person.getFullName() != null) {

            return person.getFullName().equalsIgnoreCase(selectedApplicantName);
        }

        return false;
    }

    boolean isApplicantDobMatched(HomeOfficeCaseStatus status, String appellantDateOfBirth) {

        Person person = status.getPerson();

        String applicantDob = String.format("%02d", person.getDayOfBirth())
                + String.format("%02d", person.getMonthOfBirth())
                + String.valueOf(person.getYearOfBirth()).substring(2);

        return applicantDob.equals(appellantDateOfBirth);
    }

    String getRejectionReasonString(List<RejectionReason> rejectionReasons) {
        StringBuilder sb = new StringBuilder("");
        if (rejectionReasons != null && !rejectionReasons.isEmpty()) {
            rejectionReasons
                    .forEach(rejectionReason -> sb.append(rejectionReason.getReason()).append("<br />"));
            sb.delete(sb.lastIndexOf("<br />"), sb.length());
        }
        return sb.toString();
    }
}
