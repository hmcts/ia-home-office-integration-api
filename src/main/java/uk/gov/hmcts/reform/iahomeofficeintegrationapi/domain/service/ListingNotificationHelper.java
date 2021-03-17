package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service;

import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.ADDITIONAL_TRIBUNAL_RESPONSE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.ADJOURN_HEARING_WITHOUT_DATE_REASONS;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.CASE_FLAG_SET_ASIDE_REHEARD_EXISTS;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.DECISION_HEARING_FEE_OPTION;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.IN_CAMERA_COURT_TRIBUNAL_RESPONSE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.MULTIMEDIA_TRIBUNAL_RESPONSE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.REMOTE_VIDEO_CALL_TRIBUNAL_RESPONSE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.SINGLE_SEX_COURT_TRIBUNAL_RESPONSE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.VULNERABILITIES_TRIBUNAL_RESPONSE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.WITNESS_DETAILS;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.Hearing.HearingBuilder.hearing;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HearingInstructMessage.HearingInstructMessageBuilder.hearingInstructMessage;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.MessageType.HEARING;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.MessageType.HEARING_BUNDLE_READY;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ConsumerReference;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.Hearing;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.Hearing.HearingBuilder;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HearingInstructMessage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HearingType;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.MessageHeader;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.HearingCentre;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.WitnessDetails;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.DateTimeExtractor;

@Slf4j
@Component
public class ListingNotificationHelper {

    private final DateTimeExtractor dateTimeExtractor;
    public static final String DEFAULT_HEARING_TYPE = HearingType.ORAL.toString();


    public ListingNotificationHelper(
        DateTimeExtractor dateTimeExtractor) {
        this.dateTimeExtractor = dateTimeExtractor;
    }

    public Hearing getHearingData(AsylumCase asylumCase) {

        return getHearingBuilderData(asylumCase).build();
    }

    public Hearing getHearingDataWithDate(AsylumCase asylumCase) {

        final String hearingDateTime = getHearingDateTime(asylumCase);
        final String hearingDate = dateTimeExtractor.extractHearingDate(hearingDateTime);
        final String hearingTime = dateTimeExtractor.extractHearingTime(hearingDateTime);

        return getHearingBuilderData(asylumCase)
                .withHearingDate(hearingDate)
                .withHearingTime(hearingTime)
                .build();
    }

    public HearingBuilder getHearingBuilderData(AsylumCase asylumCase) {

        final HearingBuilder hearing = hearing();

        return hearing
                .withHearingType(getHearingType(asylumCase))
                .withHmctsHearingRef(getHearingReference(asylumCase))
                .withHearingLocation(getHearingLocation(asylumCase))
                .withWitnessQty(getWitnessCount(asylumCase))
                .withWitnessNames(getWitnesses(asylumCase));
    }

    public HearingBuilder getHearingBundleReadyBuilderData(AsylumCase asylumCase) {

        final HearingBuilder hearing = hearing();

        return hearing
            .withHmctsHearingRef(getHearingReference(asylumCase))
            .withHearingType(getHearingType(asylumCase));
    }

    public HearingInstructMessage.HearingInstructMessageBuilder getHearingBuilderWithCoreFields(
        ConsumerReference consumerReference,
        MessageHeader messageHeader,
        String homeOfficeReferenceNumber) {

        return hearingInstructMessage()
            .withConsumerReference(consumerReference)
            .withHoReference(homeOfficeReferenceNumber)
            .withMessageHeader(messageHeader)
            .withMessageType(HEARING.name());
    }

    public HearingInstructMessage getAdjournHearingInstructMessage(
        AsylumCase asylumCase,
        ConsumerReference consumerReference,
        MessageHeader messageHeader,
        String homeOfficeReferenceNumber) {

        return getHearingBuilderWithCoreFields(
            consumerReference,
            messageHeader,
            homeOfficeReferenceNumber)
            .withNote(getAdjournHearingNotificationContent(asylumCase))
            .withHearing(getHearingData(asylumCase))
            .build();
    }

    public HearingInstructMessage.HearingInstructMessageBuilder getHearingBundleReadyWithCoreFields(
        ConsumerReference consumerReference,
        MessageHeader messageHeader,
        String homeOfficeReferenceNumber) {

        return hearingInstructMessage()
            .withConsumerReference(consumerReference)
            .withHoReference(homeOfficeReferenceNumber)
            .withMessageHeader(messageHeader)
            .withMessageType(HEARING_BUNDLE_READY.name());
    }

    public HearingInstructMessage getHearingBundleReadyInstructMessage(
        AsylumCase asylumCase,
        ConsumerReference consumerReference,
        MessageHeader messageHeader,
        String homeOfficeReferenceNumber) {

        return getHearingBundleReadyWithCoreFields(
            consumerReference,
            messageHeader,
            homeOfficeReferenceNumber)
            .withHearing(getHearingBundleReadyBuilderData(asylumCase).build())
            .withNote(getReheardNote(asylumCase))
            .build();
    }

    public HearingInstructMessage getHearingInstructMessage(
        AsylumCase asylumCase,
        ConsumerReference consumerReference,
        MessageHeader messageHeader,
        String homeOfficeReferenceNumber) {

        return getHearingBuilderWithCoreFields(
            consumerReference,
            messageHeader,
            homeOfficeReferenceNumber)
            .withNote(getHearingNotificationContent(asylumCase))
            .withHearing(getHearingDataWithDate(asylumCase))
            .build();
    }

    public String getHearingType(AsylumCase asylumCase) {
        final Optional<String> mayBeHearingType = asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class);

        String hearingType = DEFAULT_HEARING_TYPE;
        if (mayBeHearingType.isPresent()) {
            final String decisionHearingType = mayBeHearingType.get();
            if (decisionHearingType.equals("decisionWithHearing")) {
                hearingType = HearingType.ORAL.toString();
            } else if (decisionHearingType.equals("decisionWithoutHearing")) {
                hearingType = HearingType.PAPER.toString();
            }
        }
        return hearingType;
    }

    public String getWitnesses(AsylumCase asylumCase) {
        final Optional<List<IdValue<WitnessDetails>>> witnessDetails = asylumCase.read(WITNESS_DETAILS);

        String witnesses = null;
        if (witnessDetails.isPresent()) {
            witnesses = witnessDetails.get().stream()
                .map(idValues -> idValues.getValue().getWitnessName()).collect(Collectors.joining(", "));
        }
        return witnesses;
    }

    public Integer getWitnessCount(AsylumCase asylumCase) {
        return Integer.parseInt(asylumCase
                .read(AsylumCaseDefinition.WITNESS_COUNT, String.class).orElse("0"));
    }

    public String getHearingLocation(AsylumCase asylumCase) {
        final HearingCentre listedHearingCentre =
            asylumCase
                .read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)
                .orElseThrow(() -> new IllegalStateException("listCaseHearingCentre is not present"));

        return listedHearingCentre.toString();
    }

    public String getHearingDateTime(AsylumCase asylumCase) {
        return asylumCase
            .read(AsylumCaseDefinition.LIST_CASE_HEARING_DATE, String.class)
            .orElseThrow(() -> new IllegalStateException("hearingDateTime is not present"));
    }

    public String getHearingReference(AsylumCase asylumCase) {
        return asylumCase.read(ARIA_LISTING_REFERENCE, String.class)
                .orElseThrow(() -> new IllegalStateException("Hearing Reference is not present"));
    }

    public String getAdjournHearingNotificationContent(AsylumCase asylumCase) {

        return getReheardNote(asylumCase)
               + asylumCase.read(ADJOURN_HEARING_WITHOUT_DATE_REASONS, String.class).orElse("")
               + "\n"
               + getHearingRequirementNotificationContent(asylumCase);
    }

    public String getHearingNotificationContent(AsylumCase asylumCase) {

        return getReheardNote(asylumCase)
               + getHearingRequirementNotificationContent(asylumCase);
    }

    public String getHearingRequirementNotificationContent(AsylumCase asylumCase) {

        return "Hearing requirements:\n"
               + readStringCaseField(asylumCase, REMOTE_VIDEO_CALL_TRIBUNAL_RESPONSE,
            "Remote hearing: ",
            "No special adjustments are being made to remote video call")
               + readStringCaseField(asylumCase, VULNERABILITIES_TRIBUNAL_RESPONSE,
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

    public static String readStringCaseField(
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

    public boolean isReheardCase(AsylumCase asylumCase) {
        return asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS,
            YesOrNo.class).map(flag -> flag.equals(YesOrNo.YES)).orElse(false);
    }

    public String getReheardNote(AsylumCase asylumCase) {
        return isReheardCase(asylumCase) ? "This is a reheard case.\n" : "";
    }
}
