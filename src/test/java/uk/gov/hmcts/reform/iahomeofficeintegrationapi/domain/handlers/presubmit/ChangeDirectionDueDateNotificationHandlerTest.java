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
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.DIRECTION_EDIT_DATE_DUE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.DIRECTION_EDIT_EXPLANATION;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeInstruct;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.MessageType;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.HomeOfficeInstructService;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ChangeDirectionDueDateNotificationHandlerTest extends AbstractNotificationsHandlerTestBase {

    @Mock
    private HomeOfficeInstructService homeOfficeInstructService;

    @Captor
    private ArgumentCaptor<HomeOfficeInstruct> homeOfficeInstructMessage;

    private ChangeDirectionDueDateNotificationHandler changeDirectionDueDateNotificationHandler;

    @BeforeEach
    void setUp() {
        changeDirectionDueDateNotificationHandler =
            new ChangeDirectionDueDateNotificationHandler(
                homeOfficeInstructService, notificationsHelper
            );
    }

    @ParameterizedTest
    @CsvSource({
        "RESPONDENT_REVIEW, HOME_OFFICE_REVIEW_CHANGE_DIRECTION_DUE_DATE_INSTRUCT_STATUS",
        "AWAITING_RESPONDENT_EVIDENCE, HOME_OFFICE_EVIDENCE_CHANGE_DIRECTION_DUE_DATE_INSTRUCT_STATUS"
    })
    void check_handler_returns_case_data_for_valid_input(State state, AsylumCaseDefinition status) {

        setupCase(Event.CHANGE_DIRECTION_DUE_DATE);
        when(callback.getCaseDetails().getState()).thenReturn(state);
        setupAppealCaseData();

        when(homeOfficeInstructService.sendNotification(any(HomeOfficeInstruct.class)))
            .thenReturn("OK");

        PreSubmitCallbackResponse<AsylumCase> response =
            changeDirectionDueDateNotificationHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        assertTrue(response.getErrors().isEmpty());
        verify(asylumCase, times(1)).write(status, "OK");
        verify(homeOfficeInstructService).sendNotification(homeOfficeInstructMessage.capture());

        final HomeOfficeInstruct instructMessage = homeOfficeInstructMessage.getValue();
        assertNotificationInstructMessage(instructMessage);
    }

    private void assertNotificationInstructMessage(HomeOfficeInstruct instructMessage) {
        assertThat(instructMessage.getConsumerReference()).isEqualTo(consumerReference);
        assertThat(instructMessage.getMessageHeader()).isEqualTo(messageHeader);
        assertThat(instructMessage.getHoReference()).isEqualTo(someDocumentReference);
        assertThat(instructMessage.getMessageType()).isEqualTo(MessageType.DEFAULT.name());
        assertThat(instructMessage.getNote()).isEqualTo(
            "The due date for this direction has changed to 2020-10-10\n" + directionExplanation + "\n");
    }

    protected void setupAppealCaseData() {
        when(notificationsHelper.getHomeOfficeReference(asylumCase)).thenReturn(someDocumentReference);
        when(notificationsHelper.getCaseId(asylumCase)).thenReturn(someCaseReference);
        when(notificationsHelper.getMessageHeader()).thenReturn(messageHeader);
        when(notificationsHelper.getConsumerReference(anyString())).thenReturn(consumerReference);
        when(asylumCase.read(DIRECTION_EDIT_DATE_DUE, String.class)).thenReturn(Optional.of(dueDate));
        when(asylumCase.read(DIRECTION_EDIT_EXPLANATION, String.class)).thenReturn(Optional.of(directionExplanation));
    }

    @ParameterizedTest
    @CsvSource({
        "RESPONDENT_REVIEW, HOME_OFFICE_REVIEW_CHANGE_DIRECTION_DUE_DATE_INSTRUCT_STATUS",
        "AWAITING_RESPONDENT_EVIDENCE, HOME_OFFICE_EVIDENCE_CHANGE_DIRECTION_DUE_DATE_INSTRUCT_STATUS"
    })
    void check_handler_returns_error_status(State state, AsylumCaseDefinition status) {

        setupCase(Event.CHANGE_DIRECTION_DUE_DATE);
        when(callback.getCaseDetails().getState()).thenReturn(state);
        setupAppealCaseData();

        when(homeOfficeInstructService.sendNotification(any(HomeOfficeInstruct.class)))
            .thenReturn("FAIL");

        PreSubmitCallbackResponse<AsylumCase> response =
            changeDirectionDueDateNotificationHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        assertThat(response.getErrors()).isEmpty();
        verify(asylumCase, times(1)).write(status, "FAIL");

        verify(homeOfficeInstructService).sendNotification(homeOfficeInstructMessage.capture());
        final HomeOfficeInstruct instructMessage = homeOfficeInstructMessage.getValue();

        assertNotificationInstructMessage(instructMessage);
    }

    @Test
    void handling_should_throw_if_event_not_applicable() {

        when(callback.getEvent()).thenReturn(Event.UNKNOWN);

        assertThatThrownBy(() -> changeDirectionDueDateNotificationHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handling_should_throw_if_not_bound_to__about_to_submit__callback_stage() {

        assertThatThrownBy(() -> changeDirectionDueDateNotificationHandler.handle(ABOUT_TO_START, callback))
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

                boolean canHandle = changeDirectionDueDateNotificationHandler.canHandle(callbackStage, callback);

                if (event == Event.CHANGE_DIRECTION_DUE_DATE && callbackStage == ABOUT_TO_SUBMIT) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
        reset(callback);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> changeDirectionDueDateNotificationHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> changeDirectionDueDateNotificationHandler.canHandle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> changeDirectionDueDateNotificationHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> changeDirectionDueDateNotificationHandler.handle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
