package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.presubmit;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.valueOf;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.Arrays;
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
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AppealDecidedInstructMessage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.CourtType;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.MessageType;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.Outcome;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.FtpaAppealDecidedNote;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.HomeOfficeInstructService;



@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class LeadershipJudgeFtpaDecidedNotificationHandlerTest extends AbstractNotificationsHandlerTestBase {

    @Mock
    private HomeOfficeInstructService homeOfficeInstructService;

    @Captor
    private ArgumentCaptor<AppealDecidedInstructMessage> appealDecidedInstructMessageCaptor;

    private LeadershipJudgeFtpaDecidedNotificationHandler leadershipJudgeFtpaDecidedNotificationHandler;

    @BeforeEach
    void setUp() {
        leadershipJudgeFtpaDecidedNotificationHandler =
            new LeadershipJudgeFtpaDecidedNotificationHandler(
                homeOfficeInstructService, notificationsHelper
            );
    }

    @ParameterizedTest
    @CsvSource(
        value = {
        "appellant:granted", "appellant:partiallyGranted", "appellant:refused", "appellant:notAdmitted",
        "respondent:granted", "respondent:partiallyGranted", "respondent:refused", "respondent:notAdmitted"
        },
        delimiter = ':'
    )
    @MockitoSettings(strictness = Strictness.WARN)
    void check_handler_returns_case_data_for_valid_input(String applicantType, String ftpaDecisionOutcome) {

        setupCase(Event.LEADERSHIP_JUDGE_FTPA_DECISION);
        setupCaseData();
        setupHelperResponses();
        setupFtpaDecisionData(applicantType, ftpaDecisionOutcome);

        when(homeOfficeInstructService.sendNotification(any(AppealDecidedInstructMessage.class)))
            .thenReturn("OK");

        PreSubmitCallbackResponse<AsylumCase> response =
            leadershipJudgeFtpaDecidedNotificationHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        assertTrue(response.getErrors().isEmpty());

        verify(asylumCase, times(1)).write(
            valueOf(format("HOME_OFFICE_FTPA_%s_DECIDED_INSTRUCT_STATUS", applicantType.toUpperCase())), "OK");

        verify(homeOfficeInstructService).sendNotification(appealDecidedInstructMessageCaptor.capture());

        final AppealDecidedInstructMessage instructMessage = appealDecidedInstructMessageCaptor.getValue();

        assertNotificationInstructMessage(instructMessage, applicantType, ftpaDecisionOutcome);
    }

    @ParameterizedTest
    @CsvSource(
        value = {
            "appellant:granted", "appellant:partiallyGranted", "appellant:refused", "appellant:notAdmitted",
            "respondent:granted", "respondent:partiallyGranted", "respondent:refused", "respondent:notAdmitted"
        },
        delimiter = ':'
    )
    @MockitoSettings(strictness = Strictness.WARN)
    void check_handler_returns_error_status(String applicantType, String ftpaDecisionOutcome) {

        setupCase(Event.LEADERSHIP_JUDGE_FTPA_DECISION);
        setupCaseData();
        setupHelperResponses();
        setupFtpaDecisionData(applicantType, ftpaDecisionOutcome);
        when(homeOfficeInstructService.sendNotification(any(AppealDecidedInstructMessage.class)))
            .thenReturn("FAIL");

        PreSubmitCallbackResponse<AsylumCase> response =
            leadershipJudgeFtpaDecidedNotificationHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        assertThat(response.getErrors()).isEmpty();

        verify(asylumCase, times(1)).write(
            valueOf(format("HOME_OFFICE_FTPA_%s_DECIDED_INSTRUCT_STATUS", applicantType.toUpperCase())), "FAIL");

        verify(homeOfficeInstructService).sendNotification(appealDecidedInstructMessageCaptor.capture());

        final AppealDecidedInstructMessage instructMessage = appealDecidedInstructMessageCaptor.getValue();
        assertNotificationInstructMessage(instructMessage, applicantType, ftpaDecisionOutcome);

    }

    @Test
    void handling_should_throw_if_event_not_applicable() {

        when(callback.getEvent()).thenReturn(Event.UNKNOWN);

        assertThatThrownBy(() -> leadershipJudgeFtpaDecidedNotificationHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handling_should_throw_if_not_bound_to__about_to_submit__callback_stage() {

        assertThatThrownBy(() -> leadershipJudgeFtpaDecidedNotificationHandler.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = leadershipJudgeFtpaDecidedNotificationHandler.canHandle(callbackStage, callback);

                if (event == Event.LEADERSHIP_JUDGE_FTPA_DECISION
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

        assertThatThrownBy(() -> leadershipJudgeFtpaDecidedNotificationHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> leadershipJudgeFtpaDecidedNotificationHandler.canHandle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> leadershipJudgeFtpaDecidedNotificationHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> leadershipJudgeFtpaDecidedNotificationHandler.handle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_respond_fail_for_case_reference_null_value() {

        setupCase(Event.LEADERSHIP_JUDGE_FTPA_DECISION);
        String applicantType = "appellant";

        when(asylumCase.read(AsylumCaseDefinition.FTPA_APPLICANT_TYPE, String.class))
            .thenReturn(Optional.of(applicantType));

        PreSubmitCallbackResponse<AsylumCase> response =
            leadershipJudgeFtpaDecidedNotificationHandler.handle(ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, times(1)).write(
            valueOf(format("HOME_OFFICE_FTPA_%s_DECIDED_INSTRUCT_STATUS", applicantType.toUpperCase())), "FAIL");
    }

    private void setupFtpaDecisionData(String applicantType, String ftpaDecisionOutcome) {

        when(asylumCase.read(AsylumCaseDefinition.FTPA_APPLICANT_TYPE, String.class))
            .thenReturn(Optional.of(applicantType));

        when(asylumCase.read(
            valueOf(format("FTPA_%s_DECISION_OUTCOME_TYPE", applicantType.toUpperCase())), String.class)
        ).thenReturn(Optional.of(ftpaDecisionOutcome));
    }

    private void assertNotificationInstructMessage(
        AppealDecidedInstructMessage instructMessage, String applicantType, String ftpaDecisionOutcome) {

        assertThat(instructMessage.getConsumerReference()).isEqualTo(consumerReference);
        assertThat(instructMessage.getMessageHeader()).isEqualTo(messageHeader);
        assertThat(instructMessage.getHoReference()).isEqualTo(someDocumentReference);
        assertThat(instructMessage.getNote())
            .contains(FtpaAppealDecidedNote.fromId(ftpaDecisionOutcome + "_" + applicantType).getValue());

        assertThat(instructMessage.getMessageType()).isEqualTo(MessageType.COURT_OUTCOME.name());
        assertThat(instructMessage.getCourtOutcome().getCourtType()).isEqualTo(CourtType.FIRST_TIER);
        assertThat(instructMessage.getCourtOutcome().getOutcome()).isIn(Arrays.asList(Outcome.values()));
    }
}
