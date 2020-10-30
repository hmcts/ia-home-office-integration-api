package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_FTPA_APPELLANT_INSTRUCT_STATUS;
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
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.MessageType;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.PermissionToAppealInstructMessage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.HomeOfficeInstructService;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class FtpaAppellantNotificationHandlerTest extends AbstractNotificationsHandlerTestBase {

    @Mock
    private HomeOfficeInstructService homeOfficeInstructService;

    @Captor
    private ArgumentCaptor<PermissionToAppealInstructMessage> permissionToAppealInstructMessageArgumentCaptor;

    private FtpaAppellantNotificationHandler ftpaAppellantNotificationHandler;

    @BeforeEach
    void setUp() {

        ftpaAppellantNotificationHandler =
            new FtpaAppellantNotificationHandler(homeOfficeInstructService, notificationsHelper);
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    void check_handler_returns_case_data_for_valid_input() {

        setupCase(Event.APPLY_FOR_FTPA_APPELLANT);
        setupCaseData();
        setupHelperResponses();
        when(homeOfficeInstructService.sendNotification(any(PermissionToAppealInstructMessage.class))).thenReturn("OK");

        PreSubmitCallbackResponse<AsylumCase> response =
            ftpaAppellantNotificationHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        assertTrue(response.getErrors().isEmpty());
        verify(asylumCase, times(1)).write(HOME_OFFICE_FTPA_APPELLANT_INSTRUCT_STATUS, "OK");
        verify(homeOfficeInstructService).sendNotification(permissionToAppealInstructMessageArgumentCaptor.capture());

        final PermissionToAppealInstructMessage instructMessage =
            permissionToAppealInstructMessageArgumentCaptor.getValue();
        assertNotificationInstructMessage(instructMessage);
    }

    private void assertNotificationInstructMessage(PermissionToAppealInstructMessage instructMessage) {

        assertThat(instructMessage.getConsumerReference()).isEqualTo(consumerReference);
        assertThat(instructMessage.getMessageHeader()).isEqualTo(messageHeader);
        assertThat(instructMessage.getHoReference()).isEqualTo(someDocumentReference);
        assertThat(instructMessage.getMessageType()).isEqualTo(MessageType.PERMISSION_TO_APPEAL.toString());

        assertThat(instructMessage.getNote()).contains(
            "The appellant has submitted an application for permission to appeal to the Upper Tribunal."
        );
        assertThat(instructMessage.getNote()).contains("Next steps\n");
        assertThat(instructMessage.getNote()).contains(
            "The First-tier Tribunal will consider the application and make a decision shortly."
        );
        assertThat(instructMessage.getNote()).contains("You will be informed of the outcome.");
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    void check_handler_returns_error_status() {

        setupCase(Event.APPLY_FOR_FTPA_APPELLANT);
        setupCaseData();
        setupHelperResponses();
        when(homeOfficeInstructService.sendNotification(any(PermissionToAppealInstructMessage.class)))
            .thenReturn("FAIL");

        PreSubmitCallbackResponse<AsylumCase> response =
            ftpaAppellantNotificationHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        assertThat(response.getErrors()).isEmpty();
        verify(homeOfficeInstructService).sendNotification(permissionToAppealInstructMessageArgumentCaptor.capture());
        verify(asylumCase, times(1)).write(HOME_OFFICE_FTPA_APPELLANT_INSTRUCT_STATUS, "FAIL");

        final PermissionToAppealInstructMessage instructMessage =
            permissionToAppealInstructMessageArgumentCaptor.getValue();
        assertNotificationInstructMessage(instructMessage);
    }

    @Test
    void handling_should_throw_if_event_not_applicable() {

        when(callback.getEvent()).thenReturn(Event.UNKNOWN);

        assertThatThrownBy(() -> ftpaAppellantNotificationHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handling_should_throw_if_not_bound_to__about_to_submit__callback_stage() {

        assertThatThrownBy(() -> ftpaAppellantNotificationHandler.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = ftpaAppellantNotificationHandler.canHandle(callbackStage, callback);

                if (event == Event.APPLY_FOR_FTPA_APPELLANT
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

        assertThatThrownBy(() -> ftpaAppellantNotificationHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> ftpaAppellantNotificationHandler.canHandle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> ftpaAppellantNotificationHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> ftpaAppellantNotificationHandler.handle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_throw_error_for_case_reference_null_value() {

        setupCase(Event.APPLY_FOR_FTPA_APPELLANT);

        assertThatThrownBy(() -> ftpaAppellantNotificationHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Case ID for the appeal is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }
}
