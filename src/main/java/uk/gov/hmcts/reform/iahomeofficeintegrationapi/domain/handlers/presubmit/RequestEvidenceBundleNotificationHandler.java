package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static org.springframework.util.StringUtils.isEmpty;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.MessageType.REQUEST_EVIDENCE_BUNDLE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.RequestEvidenceBundleInstructMessage.RequestEvidenceBundleInstructMessageBuilder.requestEvidenceBundleInstructMessage;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeCaseStatus;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeChallenge;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.RequestEvidenceBundleInstructMessage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.RequestEvidenceBundleInstructMessage.RequestEvidenceBundleInstructMessageBuilder;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.HomeOfficeInstructService;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.util.HomeOfficeRequestUuidGenerator;


@Slf4j
@Component
public class RequestEvidenceBundleNotificationHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private HomeOfficeInstructService homeOfficeInstructService;

    public RequestEvidenceBundleNotificationHandler(HomeOfficeInstructService homeOfficeInstructService) {
        this.homeOfficeInstructService = homeOfficeInstructService;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && (callback.getEvent() == Event.REQUEST_RESPONDENT_EVIDENCE);
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        String homeOfficeReferenceNumber = asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)
            .orElseThrow(() -> new IllegalStateException("Home office reference for the appeal is not present"));

        final Optional<HomeOfficeCaseStatus> homeOfficeCaseStatus =
            asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_CASE_STATUS_DATA, HomeOfficeCaseStatus.class);

        if (homeOfficeCaseStatus.isPresent()) {
            String documentReference = homeOfficeCaseStatus.get().getApplicationStatus().getDocumentReference();
            if (!isEmpty(documentReference)) {
                log.info("Using document reference {} from application status instead of home{}",
                    documentReference, homeOfficeReferenceNumber);
                homeOfficeReferenceNumber = documentReference;
            }
        }

        final String caseId = asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)
            .orElseThrow(() -> new IllegalStateException("Case ID for the appeal is not present"));

        final String correlationId = HomeOfficeRequestUuidGenerator.generateUuid();

        final RequestEvidenceBundleInstructMessage bundleInstructMessage =
            buildRequest(caseId, correlationId, homeOfficeReferenceNumber, asylumCase);

        final String notificationStatus = homeOfficeInstructService.sendNotification(bundleInstructMessage);
        asylumCase.write(AsylumCaseDefinition.HOME_OFFICE_INSTRUCT_STATUS, notificationStatus);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private RequestEvidenceBundleInstructMessage buildRequest(
        String caseId, String correlationId, String homeOfficeReferenceNumber, AsylumCase asylumCase
    ) {

        RequestEvidenceBundleInstructMessageBuilder messageBuilder = requestEvidenceBundleInstructMessage();

        homeOfficeInstructService.buildCoreAttributes(
            messageBuilder, REQUEST_EVIDENCE_BUNDLE, homeOfficeReferenceNumber, caseId, correlationId
        );

        homeOfficeInstructService.extractDirectionAttributes(
            messageBuilder, asylumCase, DirectionTag.RESPONDENT_EVIDENCE);

        HomeOfficeChallenge challenge = homeOfficeInstructService.buildHomeOfficeChallenge(asylumCase);
        messageBuilder.withChallenge(challenge);

        log.info("Finished constructing {} notification request for caseId: {}, HomeOffice reference: {}",
            REQUEST_EVIDENCE_BUNDLE.toString(), caseId, homeOfficeReferenceNumber);

        return messageBuilder.build();
    }

}
