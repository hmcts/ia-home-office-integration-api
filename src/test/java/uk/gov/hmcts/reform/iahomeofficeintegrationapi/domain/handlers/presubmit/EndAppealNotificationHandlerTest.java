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
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.END_APPEAL_DATE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.END_APPEAL_OUTCOME;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.END_APPEAL_OUTCOME_REASON;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_END_APPEAL_INSTRUCT_STATUS;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.MessageType.REQUEST_CHALLENGE_END;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.EndAppealInstructMessage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.HomeOfficeInstructService;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class EndAppealNotificationHandlerTest extends AbstractNotificationsHandlerTestBase {

    @Mock
    private HomeOfficeInstructService homeOfficeInstructService;

    @Captor
    private ArgumentCaptor<EndAppealInstructMessage> endAppealInstructMessage;

    private EndAppealNotificationHandler endAppealNotificationHandler;

    @BeforeEach
    void setUp() {
        endAppealNotificationHandler =
            new EndAppealNotificationHandler(
                homeOfficeInstructService, notificationsHelper
            );
    }

    @Test
    void check_handler_returns_case_data_for_valid_input() {

        setupCase(Event.END_APPEAL);
        setupEndAppealCaseData();

        when(asylumCase.read(END_APPEAL_OUTCOME, String.class))
            .thenReturn(Optional.of("Abandoned"));
        when(homeOfficeInstructService.sendNotification(any(EndAppealInstructMessage.class)))
            .thenReturn("OK");

        PreSubmitCallbackResponse<AsylumCase> response =
            endAppealNotificationHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        assertTrue(response.getErrors().isEmpty());
        verify(asylumCase, times(1)).write(HOME_OFFICE_END_APPEAL_INSTRUCT_STATUS, "OK");
        verify(homeOfficeInstructService).sendNotification(endAppealInstructMessage.capture());

        final EndAppealInstructMessage instructMessage = endAppealInstructMessage.getValue();
        assertNotificationInstructMessage(instructMessage);
        assertThat(instructMessage.getEndReason()).isEqualTo("ABANDONED");
    }

    @Test
    void check_handler_returns_case_data_for_invalid_input() {

        setupCase(Event.END_APPEAL);
        setupEndAppealCaseData();

        when(asylumCase.read(END_APPEAL_OUTCOME, String.class))
            .thenReturn(Optional.of("Invalid"));
        when(homeOfficeInstructService.sendNotification(any(EndAppealInstructMessage.class)))
            .thenReturn("OK");

        PreSubmitCallbackResponse<AsylumCase> response =
            endAppealNotificationHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        assertTrue(response.getErrors().isEmpty());
        verify(asylumCase, times(1)).write(HOME_OFFICE_END_APPEAL_INSTRUCT_STATUS, "OK");
        verify(homeOfficeInstructService).sendNotification(endAppealInstructMessage.capture());

        final EndAppealInstructMessage instructMessage = endAppealInstructMessage.getValue();
        assertNotificationInstructMessage(instructMessage);
        assertThat(instructMessage.getEndReason()).isNull();
    }

    @Test
    void check_handler_returns_error_status() {

        setupCase(Event.END_APPEAL);
        setupEndAppealCaseData();

        when(asylumCase.read(END_APPEAL_OUTCOME, String.class))
            .thenReturn(Optional.of("Struck out"));
        when(homeOfficeInstructService.sendNotification(any(EndAppealInstructMessage.class)))
            .thenReturn("FAIL");

        PreSubmitCallbackResponse<AsylumCase> response =
            endAppealNotificationHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        assertThat(response.getErrors()).isEmpty();
        verify(asylumCase, times(1)).write(HOME_OFFICE_END_APPEAL_INSTRUCT_STATUS, "FAIL");

        verify(homeOfficeInstructService).sendNotification(endAppealInstructMessage.capture());
        final EndAppealInstructMessage instructMessage = endAppealInstructMessage.getValue();

        assertNotificationInstructMessage(instructMessage);
        assertThat(instructMessage.getEndReason()).isEqualTo("STRUCK_OUT");
    }

    private void assertNotificationInstructMessage(EndAppealInstructMessage instructMessage) {
        assertThat(instructMessage.getConsumerReference()).isEqualTo(consumerReference);
        assertThat(instructMessage.getMessageHeader()).isEqualTo(messageHeader);
        assertThat(instructMessage.getNote()).isEqualTo("someEndAppealOutcomeReason");
        assertThat(instructMessage.getHoReference()).isEqualTo(someDocumentReference);
        assertThat(instructMessage.getMessageType()).isEqualTo(REQUEST_CHALLENGE_END.toString());
    }

    protected void setupEndAppealCaseData() {

        when(notificationsHelper.getCaseId(asylumCase)).thenReturn(someCaseId);
        when(notificationsHelper.getHomeOfficeReference(asylumCase)).thenReturn(someDocumentReference);
        when(notificationsHelper.getMessageHeader()).thenReturn(messageHeader);
        when(notificationsHelper.getConsumerReference(anyString())).thenReturn(consumerReference);
        when(asylumCase.read(END_APPEAL_DATE, String.class)).thenReturn(Optional.of("2020-01-01"));
        when(asylumCase.read(END_APPEAL_OUTCOME_REASON, String.class))
            .thenReturn(Optional.of("someEndAppealOutcomeReason"));
    }

    @Test
    void handling_should_throw_if_event_not_applicable() {

        when(callback.getEvent()).thenReturn(Event.UNKNOWN);

        assertThatThrownBy(() -> endAppealNotificationHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handling_should_throw_if_not_bound_to__about_to_submit__callback_stage() {

        assertThatThrownBy(() -> endAppealNotificationHandler.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = endAppealNotificationHandler.canHandle(callbackStage, callback);

                if (event == Event.END_APPEAL
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

        assertThatThrownBy(() -> endAppealNotificationHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> endAppealNotificationHandler.canHandle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> endAppealNotificationHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> endAppealNotificationHandler.handle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_throw_error_for_case_reference_null_value() {

        setupCase(Event.END_APPEAL);

        assertThatThrownBy(() -> endAppealNotificationHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("endAppealOutcome is not present")
            .isExactlyInstanceOf(IllegalStateException.class);

    }
}
