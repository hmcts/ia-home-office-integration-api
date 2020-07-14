package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;

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
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.HomeOfficeSearchService;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.util.HomeOfficeDateFormatter;

@Slf4j
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
        Optional<HomeOfficeCaseStatus> selectedApplicant = selectMainApplicant(searchResponse.getStatus());
        if (!selectedApplicant.isPresent()) {
            log.info("Unable to find MAIN APPLICANT in Home office response");
            asylumCase.write(AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS, "FAIL");
        } else {
            Person person = selectedApplicant.get().getPerson();
            ApplicationStatus applicationStatus = selectedApplicant.get().getApplicationStatus();
            asylumCase.write(AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS, "SUCCESS");
            HomeOfficeCaseStatus selectedMainApplicant = selectedApplicant.get();
            selectedMainApplicant.setDisplayDateOfBirth(
                HomeOfficeDateFormatter.getPersonDateOfBirth(
                    person.getDayOfBirth(), person.getMonthOfBirth(), person.getYearOfBirth())
            );
            selectedMainApplicant.setDisplayDecisionDate(
                HomeOfficeDateFormatter.getIacDateTime(applicationStatus.getDecisionDate()));
            selectedMainApplicant.setDisplayDecisionSentDate(
                HomeOfficeDateFormatter.getIacDateTime(applicationStatus.getDecisionCommunication().getSentDate())
            );

            Optional<HomeOfficeMetadata> metadata = selectMetadata(applicationStatus.getHomeOfficeMetadata());
            if (metadata.isPresent()) {
                selectedMainApplicant.setDisplayMetadataValueBoolean(metadata.get().getValueBoolean());
                selectedMainApplicant.setDisplayMetadataValueDateTime(
                    HomeOfficeDateFormatter.getIacDateTime(metadata.get().getValueDateTime()));
            }
            selectedMainApplicant.setDisplayRejectionReasons(
                getRejectionReasonString(applicationStatus.getRejectionReasons()));
            asylumCase.write(AsylumCaseDefinition.HOME_OFFICE_CASE_STATUS_DATA, selectedMainApplicant);

        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    public String getRejectionReasonString(List<RejectionReason> rejectionReasons) {
        StringBuilder sb = new StringBuilder("");
        if (rejectionReasons != null && !rejectionReasons.isEmpty()) {
            rejectionReasons.forEach(
                rejectionReason -> sb.append(rejectionReason.getReason()).append("<br />")
            );
            sb.delete(sb.lastIndexOf("<br />"), sb.length());
        }
        return sb.toString();
    }

    public Optional<HomeOfficeCaseStatus> selectMainApplicant(List<HomeOfficeCaseStatus> statuses) {
        Optional<HomeOfficeCaseStatus> searchStatus = Optional.empty();
        if (statuses != null && !statuses.isEmpty()) {
            try {
                searchStatus = statuses.stream().filter(
                    status -> "APPLICANT".equalsIgnoreCase(
                        status.getApplicationStatus().getRoleType().getCode()))
                    .findFirst();
            } catch (Exception e) {
                log.info("Unable to find MAIN APPLICANT in Home office response");
            }
        }
        return searchStatus;
    }

    public Optional<HomeOfficeMetadata> selectMetadata(List<HomeOfficeMetadata> metadataList) {
        Optional<HomeOfficeMetadata> metadata = Optional.empty();
        if (metadataList != null && !metadataList.isEmpty()) {
            try {
                metadata = metadataList.stream().filter(
                    metadata1 -> "APPEALABLE".equalsIgnoreCase(metadata1.getCode()))
                    .findFirst();

            } catch (Exception e) {
                log.info("Unable to find APPEALABLE metadata in Home office response");
            }
        }
        return metadata;
    }
}
