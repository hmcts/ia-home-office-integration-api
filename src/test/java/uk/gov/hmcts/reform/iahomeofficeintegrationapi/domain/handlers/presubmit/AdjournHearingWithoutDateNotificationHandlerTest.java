package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_ADJOURN_WITHOUT_DATE_INSTRUCT_STATUS;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.MessageType.HEARING;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.Hearing;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HearingInstructMessage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.MessageHeader;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.MessageType;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.HomeOfficeInstructService;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.ListingNotificationHelper;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class AdjournHearingWithoutDateNotificationHandlerTest extends AbstractNotificationsHandlerTestBase {

    @Mock
    private HomeOfficeInstructService homeOfficeInstructService;
    @Mock
    protected ListingNotificationHelper listingNotificationHelper;
    @Mock
    protected MessageHeader messageHeader;
    @Captor
    private ArgumentCaptor<HearingInstructMessage> hearingInstructMessageArgumentCaptor;

    private AdjournHearingWithoutDateNotificationHandler adjournHearingWithoutDateNotificationHandler;

    Hearing hearing = Hearing.HearingBuilder.hearing()
        .withHearingType("ORAL")
        .withHearingDate("2020-10-01")
        .withHearingTime("10:10:10")
        .withHearingLocation("manchester")
        .withHmctsHearingRef("ariaListingReference")
        .withWitnessNames("Witness 01, Witness 02")
        .withWitnessQty(2)
        .build();

    HearingInstructMessage hearingInstructMessage =
        HearingInstructMessage.HearingInstructMessageBuilder
            .hearingInstructMessage()
            .withConsumerReference(consumerReference)
            .withHoReference(someDocumentReference)
            .withMessageHeader(messageHeader)
            .withMessageType(HEARING.name())
            .withHearing(hearing)
            .withNote("listCaseHearingDateAdjourned").build();

    HearingInstructMessage hearingInstructMessageReheard =
        HearingInstructMessage.HearingInstructMessageBuilder
            .hearingInstructMessage()
            .withConsumerReference(consumerReference)
            .withHoReference(someDocumentReference)
            .withMessageHeader(messageHeader)
            .withMessageType(HEARING.name())
            .withHearing(hearing)
            .withNote("This is a reheard case.\nlistCaseHearingDateAdjourned").build();

    @BeforeEach
    void setUp() {

        adjournHearingWithoutDateNotificationHandler =
            new AdjournHearingWithoutDateNotificationHandler(
                homeOfficeInstructService,
                notificationsHelper,
                listingNotificationHelper);
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    void check_handler_returns_case_data_for_valid_input() {

        setupCase(Event.ADJOURN_HEARING_WITHOUT_DATE);
        setupCaseData();
        setupHelperResponses();
        when(homeOfficeInstructService.sendNotification(any())).thenReturn("OK");

        when(listingNotificationHelper.getAdjournHearingInstructMessage(
                any(AsylumCase.class), any(),
                any(), anyString())).thenReturn(hearingInstructMessage);

        PreSubmitCallbackResponse<AsylumCase> response =
            adjournHearingWithoutDateNotificationHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        assertTrue(response.getErrors().isEmpty());
        verify(asylumCase, times(1)).write(HOME_OFFICE_ADJOURN_WITHOUT_DATE_INSTRUCT_STATUS, "OK");
        verify(homeOfficeInstructService).sendNotification(hearingInstructMessageArgumentCaptor.capture());

        final HearingInstructMessage instructMessage = hearingInstructMessageArgumentCaptor.getValue();
        assertNotificationInstructMessage(instructMessage);
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    void check_handler_returns_reheard_case_data_for_valid_input() {

        setupCase(Event.ADJOURN_HEARING_WITHOUT_DATE);
        setupCaseData();
        setupHelperResponses();
        when(homeOfficeInstructService.sendNotification(any())).thenReturn("OK");

        when(listingNotificationHelper.getAdjournHearingInstructMessage(
            any(AsylumCase.class), any(),
            any(), anyString())).thenReturn(hearingInstructMessageReheard);

        PreSubmitCallbackResponse<AsylumCase> response =
            adjournHearingWithoutDateNotificationHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        assertTrue(response.getErrors().isEmpty());
        verify(asylumCase, times(1)).write(HOME_OFFICE_ADJOURN_WITHOUT_DATE_INSTRUCT_STATUS, "OK");
        verify(homeOfficeInstructService).sendNotification(hearingInstructMessageArgumentCaptor.capture());

        final HearingInstructMessage instructMessage = hearingInstructMessageArgumentCaptor.getValue();
        assertThat(instructMessage.getNote()).isEqualTo("This is a reheard case.\nlistCaseHearingDateAdjourned");
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    void check_handler_returns_error_status() {

        setupCase(Event.ADJOURN_HEARING_WITHOUT_DATE);
        setupHelperResponses();
        when(homeOfficeInstructService.sendNotification(any(HearingInstructMessage.class)))
            .thenReturn("FAIL");
        when(listingNotificationHelper.getAdjournHearingInstructMessage(
                any(AsylumCase.class), any(),
                any(), anyString())).thenReturn(hearingInstructMessage);

        PreSubmitCallbackResponse<AsylumCase> response =
            adjournHearingWithoutDateNotificationHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        assertThat(response.getErrors()).isEmpty();
        verify(asylumCase, times(1)).write(HOME_OFFICE_ADJOURN_WITHOUT_DATE_INSTRUCT_STATUS, "FAIL");
        verify(homeOfficeInstructService).sendNotification(hearingInstructMessageArgumentCaptor.capture());

        final HearingInstructMessage instructMessage =
            hearingInstructMessageArgumentCaptor.getValue();
        assertNotificationInstructMessage(instructMessage);
    }

    private void assertNotificationInstructMessage(HearingInstructMessage instructMessage) {
        assertThat(instructMessage.getHoReference()).isEqualTo(someDocumentReference);
        assertThat(instructMessage.getMessageType()).isEqualTo(MessageType.HEARING.toString());
        assertThat(instructMessage.getNote()).isEqualTo("listCaseHearingDateAdjourned");

        final Hearing hearing = instructMessage.getHearing();
        assertThat(hearing.getHearingDate()).isEqualTo("2020-10-01");
        assertThat(hearing.getHearingTime()).isEqualTo("10:10:10");
        assertThat(hearing.getHearingLocation()).isEqualTo("manchester");
        assertThat(hearing.getHmctsHearingRef()).isEqualTo("ariaListingReference");
        assertThat(hearing.getWitnessQty()).isEqualTo(2);
        assertThat(hearing.getWitnessNames()).isEqualTo("Witness 01, Witness 02");
        assertThat(hearing.getHearingType()).isEqualTo("ORAL");
    }

    @Test
    void handling_should_throw_if_event_not_applicable() {

        when(callback.getEvent()).thenReturn(Event.UNKNOWN);

        assertThatThrownBy(() -> adjournHearingWithoutDateNotificationHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handling_should_throw_if_not_bound_to__about_to_submit__callback_stage() {

        assertThatThrownBy(() -> adjournHearingWithoutDateNotificationHandler.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = adjournHearingWithoutDateNotificationHandler.canHandle(callbackStage, callback);

                if (event == Event.ADJOURN_HEARING_WITHOUT_DATE
                    && callbackStage == ABOUT_TO_SUBMIT) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> adjournHearingWithoutDateNotificationHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> adjournHearingWithoutDateNotificationHandler.canHandle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> adjournHearingWithoutDateNotificationHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> adjournHearingWithoutDateNotificationHandler.handle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
