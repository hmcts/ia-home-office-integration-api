package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AppealDecidedInstructMessage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.FtpaDecidedNotificationsHelper;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.HomeOfficeInstructService;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ResidentJudgeFtpaDecidedNotificationHandlerTest extends AbstractNotificationsHandlerTestBase {

    @Mock
    private HomeOfficeInstructService homeOfficeInstructService;

    @Mock
    private FtpaDecidedNotificationsHelper ftpaDecidedNotificationsHelper;

    @Captor
    private ArgumentCaptor<AppealDecidedInstructMessage> appealDecidedInstructMessageCaptor;

    private ResidentJudgeFtpaDecidedNotificationHandler residentJudgeFtpaDecidedNotificationHandler;

    @BeforeEach
    void setUp() {
        residentJudgeFtpaDecidedNotificationHandler =
            new ResidentJudgeFtpaDecidedNotificationHandler(
                homeOfficeInstructService, notificationsHelper, ftpaDecidedNotificationsHelper
            );
    }

    @ParameterizedTest
    @ValueSource(strings = {"appellant", "respondent"})
    @MockitoSettings(strictness = Strictness.WARN)
    void check_handler_returns_case_data_for_valid_input(String applicantType) {

        setupCase(Event.RESIDENT_JUDGE_FTPA_DECISION);

        when(asylumCase.read(AsylumCaseDefinition.FTPA_APPLICANT_TYPE, String.class))
            .thenReturn(Optional.of(applicantType));

        when(ftpaDecidedNotificationsHelper.handleFtpaDecidedNotification(
            asylumCase, notificationsHelper, homeOfficeInstructService, Event.RESIDENT_JUDGE_FTPA_DECISION, "RJ_")
        ).thenReturn("OK");

        PreSubmitCallbackResponse<AsylumCase> response =
            residentJudgeFtpaDecidedNotificationHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        assertTrue(response.getErrors().isEmpty());

    }

    @ParameterizedTest
    @ValueSource(strings = {"appellant", "respondent"})
    @MockitoSettings(strictness = Strictness.WARN)
    void check_handler_returns_error_status(String applicantType) {

        setupCase(Event.RESIDENT_JUDGE_FTPA_DECISION);

        when(asylumCase.read(AsylumCaseDefinition.FTPA_APPLICANT_TYPE, String.class))
            .thenReturn(Optional.of(applicantType));

        when(ftpaDecidedNotificationsHelper.handleFtpaDecidedNotification(
            asylumCase, notificationsHelper, homeOfficeInstructService, Event.RESIDENT_JUDGE_FTPA_DECISION, "RJ_")
        ).thenReturn("FAIL");

        PreSubmitCallbackResponse<AsylumCase> response =
            residentJudgeFtpaDecidedNotificationHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        assertThat(response.getErrors()).isEmpty();
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
}
