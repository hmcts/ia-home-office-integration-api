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
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_APPEAL_DECIDED_INSTRUCT_STATUS;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AppealDecidedInstructMessage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AppealDecision;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.CourtType;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.MessageType;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.Outcome;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.AppealDecidedNote;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.HomeOfficeInstructService;



@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class AppealDecidedNotificationHandlerTest extends AbstractNotificationsHandlerTestBase {

    @Mock
    private HomeOfficeInstructService homeOfficeInstructService;

    @Captor
    private ArgumentCaptor<AppealDecidedInstructMessage> appealDecidedInstructMessageCaptor;

    private AppealDecidedNotificationHandler appealDecidedNotificationHandler;

    @BeforeEach
    void setUp() {
        appealDecidedNotificationHandler =
            new AppealDecidedNotificationHandler(
                homeOfficeInstructService, notificationsHelper
            );
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    void check_handler_returns_case_data_for_valid_input() {

        setupCase(Event.SEND_DECISION_AND_REASONS);
        setupCaseData();
        setupHelperResponses();

        when(homeOfficeInstructService.sendNotification(any(AppealDecidedInstructMessage.class)))
            .thenReturn("OK");
        when(asylumCase.read(AsylumCaseDefinition.IS_DECISION_ALLOWED, AppealDecision.class))
            .thenReturn(Optional.of(AppealDecision.ALLOWED));

        PreSubmitCallbackResponse<AsylumCase> response =
            appealDecidedNotificationHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        assertTrue(response.getErrors().isEmpty());
        verify(asylumCase, times(1)).write(HOME_OFFICE_APPEAL_DECIDED_INSTRUCT_STATUS, "OK");
        verify(homeOfficeInstructService).sendNotification(appealDecidedInstructMessageCaptor.capture());

        final AppealDecidedInstructMessage instructMessage = appealDecidedInstructMessageCaptor.getValue();
        assertNotificationInstructMessage(instructMessage);
    }

    private void assertNotificationInstructMessage(AppealDecidedInstructMessage instructMessage) {
        assertThat(instructMessage.getConsumerReference()).isEqualTo(consumerReference);
        assertThat(instructMessage.getMessageHeader()).isEqualTo(messageHeader);
        assertThat(instructMessage.getNote())
            .isIn(AppealDecidedNote.ALLOWED.getValue(), AppealDecidedNote.DISMISSED.getValue());
        assertThat(instructMessage.getHoReference()).isEqualTo(someDocumentReference);
        assertThat(instructMessage.getMessageType()).isEqualTo(MessageType.COURT_OUTCOME.name());
        assertThat(instructMessage.getCourtOutcome().getCourtType()).isEqualTo(CourtType.FIRST_TIER);
        assertThat(instructMessage.getCourtOutcome().getOutcome()).isIn(Outcome.ALLOWED, Outcome.DISMISSED);
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    void check_handler_returns_error_status() {

        setupCase(Event.SEND_DECISION_AND_REASONS);
        setupCaseData();
        setupHelperResponses();
        when(homeOfficeInstructService.sendNotification(any(AppealDecidedInstructMessage.class)))
            .thenReturn("FAIL");
        when(asylumCase.read(AsylumCaseDefinition.IS_DECISION_ALLOWED, AppealDecision.class))
            .thenReturn(Optional.of(AppealDecision.DISMISSED));

        PreSubmitCallbackResponse<AsylumCase> response =
            appealDecidedNotificationHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        assertThat(response.getErrors()).isEmpty();
        verify(asylumCase, times(1)).write(HOME_OFFICE_APPEAL_DECIDED_INSTRUCT_STATUS, "FAIL");

        verify(homeOfficeInstructService).sendNotification(appealDecidedInstructMessageCaptor.capture());
        final AppealDecidedInstructMessage instructMessage = appealDecidedInstructMessageCaptor.getValue();

        assertNotificationInstructMessage(instructMessage);

    }

    @Test
    void handling_should_throw_if_event_not_applicable() {

        when(callback.getEvent()).thenReturn(Event.UNKNOWN);

        assertThatThrownBy(() -> appealDecidedNotificationHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handling_should_throw_if_not_bound_to__about_to_submit__callback_stage() {

        assertThatThrownBy(() -> appealDecidedNotificationHandler.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = appealDecidedNotificationHandler.canHandle(callbackStage, callback);

                if (event == Event.SEND_DECISION_AND_REASONS
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

        assertThatThrownBy(() -> appealDecidedNotificationHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appealDecidedNotificationHandler.canHandle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appealDecidedNotificationHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appealDecidedNotificationHandler.handle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_throw_error_for_case_reference_null_value() {

        setupCase(Event.SEND_DECISION_AND_REASONS);

        assertThatThrownBy(() -> appealDecidedNotificationHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Case ID for the appeal is not present")
            .isExactlyInstanceOf(IllegalStateException.class);

    }
}
