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
class ResidentJudgeFtpaDecidedNotificationHandlerTest extends AbstractNotificationsHandlerTestBase {

    @Mock
    private HomeOfficeInstructService homeOfficeInstructService;

    @Captor
    private ArgumentCaptor<AppealDecidedInstructMessage> appealDecidedInstructMessageCaptor;

    private ResidentJudgeFtpaDecidedNotificationHandler residentJudgeFtpaDecidedNotificationHandler;

    @BeforeEach
    void setUp() {
        residentJudgeFtpaDecidedNotificationHandler =
            new ResidentJudgeFtpaDecidedNotificationHandler(
                homeOfficeInstructService, notificationsHelper
            );
    }

    @ParameterizedTest
    @CsvSource(
        value = {
            "appellant:granted", "appellant:partiallyGranted", "appellant:refused", "appellant:reheardRule35",
            "appellant:reheardRule32", "respondent:granted", "respondent:partiallyGranted",
            "respondent:refused", "respondent:reheardRule35", "appellant:reheardRule32"
        },
        delimiter = ':'
    )
    @MockitoSettings(strictness = Strictness.WARN)
    void check_handler_returns_case_data_for_valid_input(String applicantType, String ftpaDecisionOutcome) {

        setupCase(Event.RESIDENT_JUDGE_FTPA_DECISION);
        setupCaseData();
        setupHelperResponses();
        setupFtpaDecisionData(applicantType, ftpaDecisionOutcome);

        when(homeOfficeInstructService.sendNotification(any(AppealDecidedInstructMessage.class)))
            .thenReturn("OK");

        PreSubmitCallbackResponse<AsylumCase> response =
            residentJudgeFtpaDecidedNotificationHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        assertTrue(response.getErrors().isEmpty());

        verify(asylumCase, times(1)).write(
            valueOf(format("HOME_OFFICE_FTPA_%s_DECIDED_INSTRUCT_STATUS", applicantType.toUpperCase())), "OK");

        verify(homeOfficeInstructService).sendNotification(appealDecidedInstructMessageCaptor.capture());

        final AppealDecidedInstructMessage instructMessage = appealDecidedInstructMessageCaptor.getValue();

        assertNotificationInstructMessage(instructMessage, applicantType, ftpaDecisionOutcome, null);
    }

    @ParameterizedTest
    @CsvSource(
        value = {
            "appellant:remadeRule32:allowed", "appellant:remadeRule32:dismissed",
            "respondent:remadeRule32:allowed", "respondent:remadeRule32:dismissed"
        },
        delimiter = ':'
    )
    @MockitoSettings(strictness = Strictness.WARN)
    void check_handler_returns_case_data_for_valid_remade_input(
        String applicantType, String ftpaDecisionOutcome, String remadeDecision
    ) {

        setupCase(Event.RESIDENT_JUDGE_FTPA_DECISION);
        setupCaseData();
        setupHelperResponses();
        setupFtpaDecisionData(applicantType, ftpaDecisionOutcome);

        when(asylumCase.read(
            valueOf(format("FTPA_%s_DECISION_REMADE_RULE_32", applicantType.toUpperCase())), String.class)
        ).thenReturn(Optional.of(remadeDecision));

        when(homeOfficeInstructService.sendNotification(any(AppealDecidedInstructMessage.class)))
            .thenReturn("OK");

        PreSubmitCallbackResponse<AsylumCase> response =
            residentJudgeFtpaDecidedNotificationHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        assertTrue(response.getErrors().isEmpty());

        verify(asylumCase).read(
            valueOf(format("FTPA_%s_DECISION_REMADE_RULE_32", applicantType.toUpperCase())), String.class);

        verify(homeOfficeInstructService).sendNotification(appealDecidedInstructMessageCaptor.capture());

        verify(asylumCase, times(1)).write(
            valueOf(format("HOME_OFFICE_FTPA_%s_DECIDED_INSTRUCT_STATUS", applicantType.toUpperCase())), "OK");

        final AppealDecidedInstructMessage instructMessage = appealDecidedInstructMessageCaptor.getValue();

        assertNotificationInstructMessage(instructMessage, applicantType, ftpaDecisionOutcome, remadeDecision);
    }

    @ParameterizedTest
    @CsvSource(
        value = {
            "appellant:granted", "appellant:partiallyGranted", "appellant:refused", "appellant:reheardRule35",
            "appellant:reheardRule32", "respondent:granted", "respondent:partiallyGranted",
            "respondent:refused", "respondent:reheardRule35", "appellant:reheardRule32"
        },
        delimiter = ':'
    )
    @MockitoSettings(strictness = Strictness.WARN)
    void check_handler_returns_error_status(String applicantType, String ftpaDecisionOutcome) {

        setupCase(Event.RESIDENT_JUDGE_FTPA_DECISION);
        setupCaseData();
        setupHelperResponses();
        setupFtpaDecisionData(applicantType, ftpaDecisionOutcome);
        when(homeOfficeInstructService.sendNotification(any(AppealDecidedInstructMessage.class)))
            .thenReturn("FAIL");

        PreSubmitCallbackResponse<AsylumCase> response =
            residentJudgeFtpaDecidedNotificationHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        assertThat(response.getErrors()).isEmpty();

        verify(asylumCase, times(1)).write(
            valueOf(format("HOME_OFFICE_FTPA_%s_DECIDED_INSTRUCT_STATUS", applicantType.toUpperCase())), "FAIL");

        verify(homeOfficeInstructService).sendNotification(appealDecidedInstructMessageCaptor.capture());

        final AppealDecidedInstructMessage instructMessage = appealDecidedInstructMessageCaptor.getValue();
        assertNotificationInstructMessage(instructMessage, applicantType, ftpaDecisionOutcome, null);

    }

    @Test
    void handling_should_throw_if_event_not_applicable() {

        when(callback.getEvent()).thenReturn(Event.UNKNOWN);

        assertThatThrownBy(() -> residentJudgeFtpaDecidedNotificationHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handling_should_throw_if_not_bound_to__about_to_submit__callback_stage() {

        assertThatThrownBy(() -> residentJudgeFtpaDecidedNotificationHandler.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = residentJudgeFtpaDecidedNotificationHandler.canHandle(callbackStage, callback);

                if (event == Event.RESIDENT_JUDGE_FTPA_DECISION
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

        assertThatThrownBy(() -> residentJudgeFtpaDecidedNotificationHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> residentJudgeFtpaDecidedNotificationHandler.canHandle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> residentJudgeFtpaDecidedNotificationHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> residentJudgeFtpaDecidedNotificationHandler.handle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_respond_fail_for_case_reference_null_value() {

        setupCase(Event.RESIDENT_JUDGE_FTPA_DECISION);
        String applicantType = "appellant";

        when(asylumCase.read(AsylumCaseDefinition.FTPA_APPLICANT_TYPE, String.class))
            .thenReturn(Optional.of(applicantType));

        PreSubmitCallbackResponse<AsylumCase> response =
            residentJudgeFtpaDecidedNotificationHandler.handle(ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, times(1)).write(
            valueOf(format("HOME_OFFICE_FTPA_%s_DECIDED_INSTRUCT_STATUS", applicantType.toUpperCase())), "FAIL");
    }

    private void setupFtpaDecisionData(String applicantType, String ftpaDecisionOutcome) {

        when(asylumCase.read(AsylumCaseDefinition.FTPA_APPLICANT_TYPE, String.class))
            .thenReturn(Optional.of(applicantType));

        when(asylumCase.read(
            valueOf(format("FTPA_%s_RJ_DECISION_OUTCOME_TYPE", applicantType.toUpperCase())), String.class)
        ).thenReturn(Optional.of(ftpaDecisionOutcome));
    }

    private void assertNotificationInstructMessage(
        AppealDecidedInstructMessage instructMessage, String applicantType,
        String ftpaDecisionOutcome, String remadeDecision
    ) {

        assertThat(instructMessage.getConsumerReference()).isEqualTo(consumerReference);
        assertThat(instructMessage.getMessageHeader()).isEqualTo(messageHeader);
        assertThat(instructMessage.getHoReference()).isEqualTo(someDocumentReference);
        if (ftpaDecisionOutcome.contains("reheard")) {
            assertThat(instructMessage.getNote())
                .contains(FtpaAppealDecidedNote.fromId("reheard_" + applicantType).getValue());
        } else if (ftpaDecisionOutcome.contains("remade")) {
            assertThat(instructMessage.getNote())
                .contains(FtpaAppealDecidedNote.fromId("remade_" + remadeDecision).getValue());
        } else {
            assertThat(instructMessage.getNote())
                .contains(FtpaAppealDecidedNote.fromId(ftpaDecisionOutcome + "_" + applicantType).getValue());
        }

        assertThat(instructMessage.getMessageType()).isEqualTo(MessageType.COURT_OUTCOME.name());
        assertThat(instructMessage.getCourtOutcome().getCourtType()).isEqualTo(CourtType.FIRST_TIER);
        assertThat(instructMessage.getCourtOutcome().getOutcome()).isIn(Arrays.asList(Outcome.values()));
    }
}
