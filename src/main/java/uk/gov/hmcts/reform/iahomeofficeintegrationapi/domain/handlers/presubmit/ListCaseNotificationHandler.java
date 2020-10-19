package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.ADDITIONAL_TRIBUNAL_RESPONSE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.DECISION_HEARING_FEE_OPTION;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.IN_CAMERA_COURT_TRIBUNAL_RESPONSE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.MULTIMEDIA_TRIBUNAL_RESPONSE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.SINGLE_SEX_COURT_TRIBUNAL_RESPONSE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.VULNERABILITIES_TRIBUNAL_RESPONSE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.WITNESS_DETAILS;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.Hearing.HearingBuilder.hearing;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HearingInstructMessage.HearingInstructMessageBuilder.hearingInstructMessage;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.MessageType.HEARING;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.Hearing;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.Hearing.HearingBuilder;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HearingInstructMessage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HearingType;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.HearingCentre;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.WitnessDetails;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.HomeOfficeInstructService;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.NotificationsHelper;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.DateTimeExtractor;


@Slf4j
@Component
public class ListCaseNotificationHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final HomeOfficeInstructService homeOfficeInstructService;
    private final NotificationsHelper notificationsHelper;
    private final DateTimeExtractor dateTimeExtractor;
    public static final String DEFAULT_HEARING_TYPE = HearingType.ORAL.toString();

    public ListCaseNotificationHandler(
        HomeOfficeInstructService homeOfficeInstructService,
        NotificationsHelper notificationsHelper,
        DateTimeExtractor dateTimeExtractor) {
        this.homeOfficeInstructService = homeOfficeInstructService;
        this.notificationsHelper = notificationsHelper;
        this.dateTimeExtractor = dateTimeExtractor;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && Arrays.asList(
                Event.LIST_CASE,
                Event.EDIT_CASE_LISTING
                ).contains(callback.getEvent());
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        log.info("Preparing to send {} notification to HomeOffice", HEARING.toString());

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        final String homeOfficeReferenceNumber = notificationsHelper.getHomeOfficeReference(asylumCase);

        final String caseId = asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)
            .orElseThrow(() -> new IllegalStateException("Case ID for the appeal is not present"));

        final HearingInstructMessage bundleInstructMessage =
            hearingInstructMessage()
                .withConsumerReference(notificationsHelper.getConsumerReference(caseId))
                .withHoReference(homeOfficeReferenceNumber)
                .withMessageHeader(notificationsHelper.getMessageHeader())
                .withMessageType(HEARING.name())
                .withNote(getHearingNotificationContent(asylumCase))
                .withHearing(getHearingData(asylumCase))
                .build();

        log.info("Finished constructing {} notification request for caseId: {}, HomeOffice reference: {}",
            HEARING.toString(), caseId, homeOfficeReferenceNumber);

        final String notificationStatus = homeOfficeInstructService.sendNotification(bundleInstructMessage);

        if (callback.getEvent().equals(Event.EDIT_CASE_LISTING)) {
            asylumCase.write(AsylumCaseDefinition.HOME_OFFICE_EDIT_LISTING_INSTRUCT_STATUS, notificationStatus);
        } else {
            asylumCase.write(AsylumCaseDefinition.HOME_OFFICE_HEARING_INSTRUCT_STATUS, notificationStatus);
        }

        log.info("SENT: {} Event for {} notification for caseId: {}, HomeOffice reference: {}, status: {}",
            callback.getEvent(), HEARING.toString(), caseId, homeOfficeReferenceNumber, notificationStatus);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private Hearing getHearingData(AsylumCase asylumCase) {

        final HearingCentre listedHearingCentre =
            asylumCase
                .read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)
                .orElseThrow(() -> new IllegalStateException("listCaseHearingCentre is not present"));

        final String hearingLocation = listedHearingCentre.getValue();

        final String hearingDateTime =
            asylumCase
                .read(AsylumCaseDefinition.LIST_CASE_HEARING_DATE, String.class)
                .orElseThrow(() -> new IllegalStateException("hearingDateTime is not present"));

        final String hearingDate = dateTimeExtractor.extractHearingDate(hearingDateTime);
        final String hearingTime = dateTimeExtractor.extractHearingTime(hearingDateTime);

        final int witnessCount = Integer.parseInt(asylumCase
            .read(AsylumCaseDefinition.WITNESS_COUNT, String.class).orElse("0"));

        final Optional<List<IdValue<WitnessDetails>>> witnessDetails = asylumCase.read(WITNESS_DETAILS);

        String witnesses = null;
        if (witnessDetails.isPresent()) {
            witnesses = witnessDetails.get().stream()
                .map(idValues -> idValues.getValue().getWitnessName()).collect(Collectors.joining(", "));
        }

        final String hmctsHearingRef = asylumCase.read(ARIA_LISTING_REFERENCE, String.class)
            .orElseThrow(() -> new IllegalStateException("Hearing Reference is not present"));

        final Optional<String> mayBeHearingType = asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class);

        final HearingBuilder hearing = hearing();
        if (mayBeHearingType.isPresent()) {
            final String hearingType = mayBeHearingType.get();
            if (hearingType.equals("decisionWithHearing")) {
                hearing.withHearingType(HearingType.ORAL.toString());
            } else if (hearingType.equals("decisionWithoutHearing")) {
                hearing.withHearingType(HearingType.PAPER.toString());
            }
        } else {
            hearing.withHearingType(DEFAULT_HEARING_TYPE);
        }

        return
            hearing
                .withHmctsHearingRef(hmctsHearingRef)
                .withHearingDate(hearingDate)
                .withHearingTime(hearingTime)
                .withHearingLocation(hearingLocation)
                .withWitnessQty(witnessCount)
                .withWitnessNames(witnesses)
                .build();

    }

    private String getHearingNotificationContent(AsylumCase asylumCase) {

        return "Hearing requirements:\n" + readStringCaseField(asylumCase, VULNERABILITIES_TRIBUNAL_RESPONSE,
            "Adjustments to accommodate vulnerabilities: ",
            "No special adjustments are being made to accommodate vulnerabilities")
               + readStringCaseField(asylumCase, MULTIMEDIA_TRIBUNAL_RESPONSE, "Multimedia equipment: ",
            "No multimedia equipment is being provided")
               + readStringCaseField(asylumCase, SINGLE_SEX_COURT_TRIBUNAL_RESPONSE, "Single-sex court: ",
            "The court will not be single sex")
               + readStringCaseField(asylumCase, IN_CAMERA_COURT_TRIBUNAL_RESPONSE, "In camera court: ",
            "The hearing will be held in public court")
               + readStringCaseField(asylumCase, ADDITIONAL_TRIBUNAL_RESPONSE, "Other adjustments: ",
            "No other adjustments are being made");
    }

    private static String readStringCaseField(
        final AsylumCase asylumCase, final AsylumCaseDefinition caseField,
        final String label, final String defaultIfNotPresent) {

        final Optional<String> optionalFieldValue = asylumCase.read(caseField, String.class);
        return
            "* "
            + label
            + (optionalFieldValue.isPresent() && !optionalFieldValue.get().isEmpty()
                ? optionalFieldValue.get()
                : defaultIfNotPresent)
            + "\n";
    }
}
