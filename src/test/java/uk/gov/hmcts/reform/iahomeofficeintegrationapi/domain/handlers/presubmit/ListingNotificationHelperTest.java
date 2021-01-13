package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.ADDITIONAL_TRIBUNAL_RESPONSE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.ADJOURN_HEARING_WITHOUT_DATE_REASONS;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.DECISION_HEARING_FEE_OPTION;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.IN_CAMERA_COURT_TRIBUNAL_RESPONSE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.MULTIMEDIA_TRIBUNAL_RESPONSE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.SINGLE_SEX_COURT_TRIBUNAL_RESPONSE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.VULNERABILITIES_TRIBUNAL_RESPONSE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.WITNESS_COUNT;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.WITNESS_DETAILS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ConsumerReference;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.Hearing;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HearingInstructMessage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.MessageHeader;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.HearingCentre;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.WitnessDetails;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.ListingNotificationHelper;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.DateTimeExtractor;


@ExtendWith(MockitoExtension.class)
class ListingNotificationHelperTest {

    @Mock
    protected DateTimeExtractor dateTimeExtractor;
    @Mock
    protected AsylumCase asylumCase;
    @Mock
    protected ConsumerReference consumerReference;
    @Mock
    protected MessageHeader messageHeader;

    ListingNotificationHelper listingNotificationHelper;

    Hearing.HearingBuilder hearingData = Hearing.HearingBuilder.hearing()
        .withHearingType("ORAL")
        .withHearingLocation("Manchester")
        .withHmctsHearingRef("ariaListingReference")
        .withWitnessNames("Witness 01, Witness 02")
        .withWitnessQty(2);

    @BeforeEach
    public void setUp() {

        listingNotificationHelper = new ListingNotificationHelper(dateTimeExtractor);

        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class))
            .thenReturn(Optional.of("decisionWithHearing"));

        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class))
            .thenReturn(Optional.of("ariaListingReference"));

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
            .thenReturn(Optional.of(HearingCentre.MANCHESTER));

        when(asylumCase.read(WITNESS_COUNT, String.class))
            .thenReturn(Optional.of("2"));

        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class))
            .thenReturn(Optional.of("2018-12-31T12:34:56"));

        List<IdValue<WitnessDetails>> witnessDetails = new ArrayList<>();
        Collections.addAll(witnessDetails,
            new IdValue<>("0", new WitnessDetails("Witness 01")),
            new IdValue<>("1", new WitnessDetails("Witness 02"))
        );

        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(witnessDetails));
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    void shouldGetHearingDataWithDate() {

        String hearingDateTime = listingNotificationHelper.getHearingDateTime(asylumCase);
        when(dateTimeExtractor.extractHearingDate(hearingDateTime)).thenReturn("2018-12-31");
        when(dateTimeExtractor.extractHearingTime(hearingDateTime)).thenReturn("12:34:56");

        Hearing hearing = listingNotificationHelper.getHearingDataWithDate(asylumCase);

        assertEquals("2018-12-31", hearing.getHearingDate());
        assertEquals("12:34:56", hearing.getHearingTime());
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    void shouldGetHearingBuilderData() {

        Hearing.HearingBuilder hearingBuilder = listingNotificationHelper.getHearingBuilderData(asylumCase);

        assertEquals(hearingData.build().getHearingType(), hearingBuilder.build().getHearingType());
        assertEquals(hearingData.build().getHmctsHearingRef(), hearingBuilder.build().getHmctsHearingRef());
        assertEquals(hearingData.build().getHearingLocation(), hearingBuilder.build().getHearingLocation());
        assertEquals(hearingData.build().getWitnessQty(), hearingBuilder.build().getWitnessQty());
        assertEquals(hearingData.build().getWitnessNames(), hearingBuilder.build().getWitnessNames());
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    void shouldGetAdjournHearingInstructMessage() {

        when(asylumCase.read(ADJOURN_HEARING_WITHOUT_DATE_REASONS, String.class))
            .thenReturn(Optional.of("Some reason for adjourn hearing without date"));

        HearingInstructMessage hearingInstructMessage
            = listingNotificationHelper.getAdjournHearingInstructMessage(
                asylumCase, consumerReference, messageHeader, "1111-2222-3333-4444");

        assertEquals("ORAL", hearingInstructMessage.getHearing().getHearingType());
        assertEquals("ariaListingReference", hearingInstructMessage.getHearing().getHmctsHearingRef());
        assertEquals("Manchester", hearingInstructMessage.getHearing().getHearingLocation());
        assertEquals("HEARING", hearingInstructMessage.getMessageType());
        assertEquals(messageHeader, hearingInstructMessage.getMessageHeader());
        assertEquals(consumerReference, hearingInstructMessage.getConsumerReference());
        assertEquals("Some reason for adjourn hearing without date\nHearing requirements:\n"
                     + "* Adjustments to accommodate vulnerabilities: "
                     + "No special adjustments are being made to accommodate vulnerabilities\n"
                     + "* Multimedia equipment: No multimedia equipment is being provided\n"
                     + "* Single-sex court: The court will not be single sex\n"
                     + "* In camera court: The hearing will be held in public court\n"
                     + "* Other adjustments: No other adjustments are being made\n",
            hearingInstructMessage.getNote());
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    void shouldGetHearingBundleReadyInstructMessage() {

        HearingInstructMessage hearingBundleReadyInstructMessage
            = listingNotificationHelper.getHearingBundleReadyInstructMessage(asylumCase,
            consumerReference, messageHeader, "1111-2222-3333-4444");

        assertEquals("ORAL", hearingBundleReadyInstructMessage.getHearing().getHearingType());
        assertEquals("ariaListingReference", hearingBundleReadyInstructMessage.getHearing().getHmctsHearingRef());
        assertEquals("HEARING_BUNDLE_READY", hearingBundleReadyInstructMessage.getMessageType());
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    void shouldGetHearingInstructMessage() {

        HearingInstructMessage hearingInstructMessage
            = listingNotificationHelper.getHearingInstructMessage(asylumCase,
                consumerReference, messageHeader, "1111-2222-3333-4444");

        assertHearingDataWithContent(hearingInstructMessage);
    }

    private void assertHearingDataWithContent(HearingInstructMessage hearingInstructMessage) {
        assertEquals("ORAL", hearingInstructMessage.getHearing().getHearingType());
        assertEquals("ariaListingReference", hearingInstructMessage.getHearing().getHmctsHearingRef());
        assertEquals("Manchester", hearingInstructMessage.getHearing().getHearingLocation());
        assertEquals("HEARING", hearingInstructMessage.getMessageType());
        assertEquals(messageHeader, hearingInstructMessage.getMessageHeader());
        assertEquals(consumerReference, hearingInstructMessage.getConsumerReference());
        assertEquals("Hearing requirements:\n"
                     + "* Adjustments to accommodate vulnerabilities: "
                     + "No special adjustments are being made to accommodate vulnerabilities\n"
                     + "* Multimedia equipment: No multimedia equipment is being provided\n"
                     + "* Single-sex court: The court will not be single sex\n"
                     + "* In camera court: The hearing will be held in public court\n"
                     + "* Other adjustments: No other adjustments are being made\n",
            hearingInstructMessage.getNote());
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    void shouldGetHearingBuilderWithCoreFields() {

        HearingInstructMessage.HearingInstructMessageBuilder hearingInstructMessageBuilder
            = listingNotificationHelper.getHearingBuilderWithCoreFields(
                consumerReference, messageHeader, "1111-2222-3333-4444");

        assertEquals("1111-2222-3333-4444", hearingInstructMessageBuilder.build().getHoReference());
        assertEquals(consumerReference, hearingInstructMessageBuilder.build().getConsumerReference());
        assertEquals(messageHeader, hearingInstructMessageBuilder.build().getMessageHeader());
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    void shouldGetWitness() {

        String witnessDetails = listingNotificationHelper.getWitnesses(asylumCase);
        assertEquals("Witness 01, Witness 02", witnessDetails);
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    void shouldBeNullForNoWitness() {

        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.empty());
        String witnessDetails = listingNotificationHelper.getWitnesses(asylumCase);
        assertEquals(null, witnessDetails);
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    void shouldGetWitnessCount() {

        int witnessCount = listingNotificationHelper.getWitnessCount(asylumCase);
        assertEquals(2, witnessCount);
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    void shouldBeZeroForNoWitness() {

        when(asylumCase.read(WITNESS_COUNT, String.class))
            .thenReturn(Optional.empty());

        int witnessCount = listingNotificationHelper.getWitnessCount(asylumCase);
        assertEquals(0, witnessCount);
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    void shouldGetHearingLocation() {

        String listedHearingCentre = listingNotificationHelper.getHearingLocation(asylumCase);
        assertEquals("Manchester", listedHearingCentre);
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    void shouldThrowErrorEmptyHearingLocation() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> listingNotificationHelper.getHearingLocation(asylumCase))
            .hasMessage("listCaseHearingCentre is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    void shouldGetHearingDateTime() {

        String hearingDateTime = listingNotificationHelper.getHearingDateTime(asylumCase);
        assertEquals("2018-12-31T12:34:56", hearingDateTime);
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    void shouldThrowErrorEmptyHearingDateTime() {

        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> listingNotificationHelper.getHearingDateTime(asylumCase))
            .hasMessage("hearingDateTime is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    void shouldGetHearingReference() {

        String hearingReference = listingNotificationHelper.getHearingReference(asylumCase);
        assertEquals("ariaListingReference", hearingReference);
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    void shouldThrowErrorEmptyHearingReference() {

        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> listingNotificationHelper.getHearingReference(asylumCase))
            .hasMessage("Hearing Reference is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    void shouldGetHearingTypeForDecisionWithHearing() {

        String hearingType = listingNotificationHelper.getHearingType(asylumCase);
        assertEquals("ORAL", hearingType);
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    void shouldGetHearingTypeForDecisionWithoutHearing() {

        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class))
            .thenReturn(Optional.of("decisionWithoutHearing"));

        String hearingType = listingNotificationHelper.getHearingType(asylumCase);
        assertEquals("PAPER", hearingType);
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    void shouldGetDefaultHearingTypeForEmptyData() {

        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class))
            .thenReturn(Optional.of(""));

        String hearingType = listingNotificationHelper.getHearingType(asylumCase);
        assertEquals("ORAL", hearingType);
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    void shouldAdjournHearingNotificationContent() {

        when(asylumCase.read(ADJOURN_HEARING_WITHOUT_DATE_REASONS, String.class))
            .thenReturn(Optional.of("Some reason for adjourn hearing without date"));

        final String adjournHearingNotificationContent
            = listingNotificationHelper.getAdjournHearingNotificationContent(asylumCase);

        assertEquals("Some reason for adjourn hearing without date\nHearing requirements:\n"
                     + "* Adjustments to accommodate vulnerabilities: "
                     + "No special adjustments are being made to accommodate vulnerabilities\n"
                     + "* Multimedia equipment: No multimedia equipment is being provided\n"
                     + "* Single-sex court: The court will not be single sex\n"
                     + "* In camera court: The hearing will be held in public court\n"
                     + "* Other adjustments: No other adjustments are being made\n",
            adjournHearingNotificationContent);
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    void shouldExtractHearingNotificationContent() {

        when(asylumCase.read(VULNERABILITIES_TRIBUNAL_RESPONSE, String.class))
            .thenReturn(Optional.of("something around Vulnerabilities"));
        when(asylumCase.read(MULTIMEDIA_TRIBUNAL_RESPONSE, String.class))
            .thenReturn(Optional.of("something around Multimedia"));
        when(asylumCase.read(SINGLE_SEX_COURT_TRIBUNAL_RESPONSE, String.class))
            .thenReturn(Optional.of("something around SingleSexCourt"));
        when(asylumCase.read(IN_CAMERA_COURT_TRIBUNAL_RESPONSE, String.class))
            .thenReturn(Optional.of("something around InCameraCourt"));
        when(asylumCase.read(ADDITIONAL_TRIBUNAL_RESPONSE, String.class))
            .thenReturn(Optional.of("something around Other"));

        final String hearingNotificationContent = listingNotificationHelper.getHearingNotificationContent(asylumCase);

        assertEquals("Hearing requirements:\n"
            + "* Adjustments to accommodate vulnerabilities: something around Vulnerabilities\n"
            + "* Multimedia equipment: something around Multimedia\n"
            + "* Single-sex court: something around SingleSexCourt\n"
            + "* In camera court: something around InCameraCourt\n"
            + "* Other adjustments: something around Other\n",
            hearingNotificationContent);
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    void shouldReadStringCaseFieldWithFieldValuePresent() {

        when(asylumCase.read(IN_CAMERA_COURT_TRIBUNAL_RESPONSE, String.class))
            .thenReturn(Optional.of("something around InCameraCourt"));

        final String readStringVulnerabilityCaseFieldValue = listingNotificationHelper
            .readStringCaseField(asylumCase,
                IN_CAMERA_COURT_TRIBUNAL_RESPONSE,
                "In camera court: ",
                "The hearing will be held in public court");

        assertEquals("* In camera court: something around InCameraCourt\n",
            readStringVulnerabilityCaseFieldValue);
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    void shouldReadStringCaseFieldWithFieldValueNotPresent() {

        final String readStringMultimediaCaseFieldValue = listingNotificationHelper
            .readStringCaseField(asylumCase,
                MULTIMEDIA_TRIBUNAL_RESPONSE,
                "Multimedia equipment: ",
                "No multimedia equipment is being provided");

        assertEquals("* Multimedia equipment: No multimedia equipment is being provided\n",
            readStringMultimediaCaseFieldValue);
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    void shouldReadStringCaseFieldWithFieldValuePresentButEmpty() {

        when(asylumCase.read(MULTIMEDIA_TRIBUNAL_RESPONSE, String.class))
            .thenReturn(Optional.of(""));

        final String readStringMultimediaCaseFieldValue = listingNotificationHelper
            .readStringCaseField(asylumCase,
                MULTIMEDIA_TRIBUNAL_RESPONSE,
                "Multimedia equipment: ",
                "No multimedia equipment is being provided");

        assertEquals("* Multimedia equipment: No multimedia equipment is being provided\n",
            readStringMultimediaCaseFieldValue);
    }
}
