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
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_HEARING_BUNDLE_READY_INSTRUCT_STATUS;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.MessageType.HEARING_BUNDLE_READY;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.Hearing;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HearingInstructMessage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.HomeOfficeInstructService;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.ListingNotificationHelper;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class HearingBundleReadyNotificationHandlerTest extends AbstractNotificationsHandlerTestBase {

    @Mock
    private HomeOfficeInstructService homeOfficeInstructService;
    @Mock
    protected ListingNotificationHelper listingNotificationHelper;

    @Captor
    private ArgumentCaptor<HearingInstructMessage> hearingInstructMessageArgumentCaptor;

    private HearingBundleReadyNotificationHandler hearingBundleReadyNotificationHandler;

    Hearing hearing = Hearing.HearingBuilder.hearing()
        .withHearingType("ORAL")
        .withHmctsHearingRef("ariaListingReference")
        .build();

    HearingInstructMessage hearingInstructMessage =
        HearingInstructMessage.HearingInstructMessageBuilder
            .hearingInstructMessage()
            .withConsumerReference(consumerReference)
            .withHoReference(someDocumentReference)
            .withMessageHeader(messageHeader)
            .withMessageType(HEARING_BUNDLE_READY.name())
            .withHearing(hearing).build();

    @BeforeEach
    void setUp() {

        hearingBundleReadyNotificationHandler =
            new HearingBundleReadyNotificationHandler(
                homeOfficeInstructService,
                notificationsHelper,
                listingNotificationHelper);
    }

    @Test
    void check_handler_returns_case_data_for_valid_input() {

        setupCase(Event.ASYNC_STITCHING_COMPLETE);
        setupHelperResponses();
        when(notificationsHelper.getCaseId(any(AsylumCase.class))).thenReturn(someCaseReference);

        when(homeOfficeInstructService.sendNotification(any())).thenReturn("OK");

        when(listingNotificationHelper.getHearingBundleReadyInstructMessage(
                any(AsylumCase.class), any(),
                any(), anyString())).thenReturn(hearingInstructMessage);

        PreSubmitCallbackResponse<AsylumCase> response =
            hearingBundleReadyNotificationHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        assertTrue(response.getErrors().isEmpty());
        verify(asylumCase, times(1)).write(HOME_OFFICE_HEARING_BUNDLE_READY_INSTRUCT_STATUS, "OK");
        verify(homeOfficeInstructService).sendNotification(hearingInstructMessageArgumentCaptor.capture());

        final HearingInstructMessage instructMessage = hearingInstructMessageArgumentCaptor.getValue();
        assertNotificationInstructMessage(instructMessage);
    }

    @Test
    void check_handler_returns_error_status() {

        setupCase(Event.ASYNC_STITCHING_COMPLETE);
        setupHelperResponses();
        when(notificationsHelper.getCaseId(any(AsylumCase.class))).thenReturn(someCaseReference);

        when(homeOfficeInstructService.sendNotification(any(HearingInstructMessage.class)))
            .thenReturn("FAIL");
        when(listingNotificationHelper.getHearingBundleReadyInstructMessage(
                any(AsylumCase.class), any(),
                any(), anyString())).thenReturn(hearingInstructMessage);

        PreSubmitCallbackResponse<AsylumCase> response =
            hearingBundleReadyNotificationHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        assertThat(response.getErrors()).isEmpty();
        verify(asylumCase, times(1)).write(HOME_OFFICE_HEARING_BUNDLE_READY_INSTRUCT_STATUS, "FAIL");
        verify(homeOfficeInstructService).sendNotification(hearingInstructMessageArgumentCaptor.capture());

        final HearingInstructMessage instructMessage =
            hearingInstructMessageArgumentCaptor.getValue();
        assertNotificationInstructMessage(instructMessage);
    }

    private void assertNotificationInstructMessage(HearingInstructMessage instructMessage) {
        assertThat(instructMessage.getHoReference()).isEqualTo(someDocumentReference);
        assertThat(instructMessage.getMessageType()).isEqualTo(HEARING_BUNDLE_READY.toString());

        final Hearing hearing = instructMessage.getHearing();
        assertThat(hearing.getHmctsHearingRef()).isEqualTo("ariaListingReference");
        assertThat(hearing.getHearingType()).isEqualTo("ORAL");
    }

    @Test
    void handling_should_throw_if_event_not_applicable() {

        when(callback.getEvent()).thenReturn(Event.UNKNOWN);

        assertThatThrownBy(() -> hearingBundleReadyNotificationHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handling_should_throw_if_not_bound_to__about_to_submit__callback_stage() {

        assertThatThrownBy(() -> hearingBundleReadyNotificationHandler.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = hearingBundleReadyNotificationHandler.canHandle(callbackStage, callback);

                if ((event == Event.ASYNC_STITCHING_COMPLETE)
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

        assertThatThrownBy(() -> hearingBundleReadyNotificationHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> hearingBundleReadyNotificationHandler.canHandle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> hearingBundleReadyNotificationHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> hearingBundleReadyNotificationHandler.handle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
