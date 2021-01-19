package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_AMEND_BUNDLE_INSTRUCT_STATUS;
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
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeInstruct;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.MessageType;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.HomeOfficeInstructService;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class AmendBundleNotificationHandlerTest extends AbstractNotificationsHandlerTestBase {

    @Mock
    private HomeOfficeInstructService homeOfficeInstructService;

    @Captor
    private ArgumentCaptor<HomeOfficeInstruct> homeOfficeInstructMessage;

    private AmendBundleNotificationHandler amendBundleNotificationHandler;

    @BeforeEach
    void setUp() {
        amendBundleNotificationHandler =
            new AmendBundleNotificationHandler(
                homeOfficeInstructService, notificationsHelper
            );
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    void check_handler_returns_case_data_for_valid_input() {

        setupCase(Event.SEND_DIRECTION);
        setupAppealCaseData();
        setupHelperDirection(DirectionTag.NONE);
        when(homeOfficeInstructService.sendNotification(any(HomeOfficeInstruct.class)))
            .thenReturn("OK");

        PreSubmitCallbackResponse<AsylumCase> response =
            amendBundleNotificationHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        assertTrue(response.getErrors().isEmpty());
        verify(asylumCase, times(1)).write(HOME_OFFICE_AMEND_BUNDLE_INSTRUCT_STATUS, "OK");
        verify(homeOfficeInstructService).sendNotification(homeOfficeInstructMessage.capture());

        final HomeOfficeInstruct instructMessage = homeOfficeInstructMessage.getValue();
        assertNotificationInstructMessage(instructMessage);
    }

    private void assertNotificationInstructMessage(HomeOfficeInstruct instructMessage) {
        assertThat(instructMessage.getConsumerReference()).isEqualTo(consumerReference);
        assertThat(instructMessage.getMessageHeader()).isEqualTo(messageHeader);
        assertThat(instructMessage.getNote()).isEqualTo(directionExplanation);
        assertThat(instructMessage.getHoReference()).isEqualTo(someDocumentReference);
        assertThat(instructMessage.getMessageType()).isEqualTo(MessageType.DEFAULT.name());
    }

    protected void setupAppealCaseData() {

        when(caseDetails.getState()).thenReturn(State.AWAITING_RESPONDENT_EVIDENCE);
        when(notificationsHelper.getHomeOfficeReference(asylumCase)).thenReturn(someDocumentReference);
        when(notificationsHelper.getCaseId(asylumCase)).thenReturn(someCaseReference);
        when(notificationsHelper.getMessageHeader()).thenReturn(messageHeader);
        when(notificationsHelper.getConsumerReference(anyString())).thenReturn(consumerReference);
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    void check_handler_returns_error_status() {

        setupCase(Event.SEND_DIRECTION);
        setupAppealCaseData();
        setupHelperDirection(DirectionTag.NONE);
        when(homeOfficeInstructService.sendNotification(any(HomeOfficeInstruct.class)))
            .thenReturn("FAIL");

        PreSubmitCallbackResponse<AsylumCase> response =
            amendBundleNotificationHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        assertThat(response.getErrors()).isEmpty();
        verify(asylumCase, times(1)).write(HOME_OFFICE_AMEND_BUNDLE_INSTRUCT_STATUS, "FAIL");

        verify(homeOfficeInstructService).sendNotification(homeOfficeInstructMessage.capture());
        final HomeOfficeInstruct instructMessage = homeOfficeInstructMessage.getValue();

        assertNotificationInstructMessage(instructMessage);
    }

    @Test
    void handling_should_throw_if_event_not_applicable() {

        when(callback.getEvent()).thenReturn(Event.UNKNOWN);

        assertThatThrownBy(() -> amendBundleNotificationHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handling_should_throw_if_not_bound_to__about_to_submit__callback_stage() {

        assertThatThrownBy(() -> amendBundleNotificationHandler.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    void it_can_handle_callback() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                for (State state : State.values()) {

                    when(callback.getCaseDetails().getState()).thenReturn(state);

                    boolean canHandle = amendBundleNotificationHandler.canHandle(callbackStage, callback);

                    if (event == Event.SEND_DIRECTION && callbackStage == ABOUT_TO_SUBMIT
                        && state == State.AWAITING_RESPONDENT_EVIDENCE) {

                        assertTrue(canHandle);
                    } else {
                        assertFalse(canHandle);
                    }
                }
            }
        }
        reset(callback);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> amendBundleNotificationHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> amendBundleNotificationHandler.canHandle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> amendBundleNotificationHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> amendBundleNotificationHandler.handle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
