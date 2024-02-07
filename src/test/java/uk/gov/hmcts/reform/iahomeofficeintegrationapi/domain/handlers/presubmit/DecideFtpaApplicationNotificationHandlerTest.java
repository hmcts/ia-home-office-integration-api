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
import org.junit.jupiter.params.provider.ValueSource;;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.FtpaDecidedNotificationsHelper;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.HomeOfficeInstructService;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class DecideFtpaApplicationNotificationHandlerTest extends AbstractNotificationsHandlerTestBase {

    @Mock
    private HomeOfficeInstructService homeOfficeInstructService;

    @Mock
    private FtpaDecidedNotificationsHelper ftpaDecidedNotificationsHelper;

    private DecideFtpaApplicationNotificationHandler decideFtpaApplicationNotificationHandler;

    @BeforeEach
    void setUp() {
        decideFtpaApplicationNotificationHandler =
            new DecideFtpaApplicationNotificationHandler(
                homeOfficeInstructService, notificationsHelper, ftpaDecidedNotificationsHelper
            );
    }

    @ParameterizedTest
    @ValueSource(strings = {"appellant", "respondent"})
    @MockitoSettings(strictness = Strictness.WARN)
    void check_handler_returns_case_data_for_valid_input(String applicantType) {

        setupCase(Event.DECIDE_FTPA_APPLICATION);

        when(asylumCase.read(AsylumCaseDefinition.FTPA_APPLICANT_TYPE, String.class))
            .thenReturn(Optional.of(applicantType));

        when(ftpaDecidedNotificationsHelper.handleFtpaDecidedNotification(
            asylumCase, notificationsHelper, homeOfficeInstructService, Event.RESIDENT_JUDGE_FTPA_DECISION, "RJ_")
        ).thenReturn("OK");

        PreSubmitCallbackResponse<AsylumCase> response =
            decideFtpaApplicationNotificationHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        assertTrue(response.getErrors().isEmpty());

    }

    @ParameterizedTest
    @ValueSource(strings = {"appellant", "respondent"})
    @MockitoSettings(strictness = Strictness.WARN)
    void check_handler_returns_error_status(String applicantType) {

        setupCase(Event.DECIDE_FTPA_APPLICATION);

        when(asylumCase.read(AsylumCaseDefinition.FTPA_APPLICANT_TYPE, String.class))
            .thenReturn(Optional.of(applicantType));

        when(ftpaDecidedNotificationsHelper.handleFtpaDecidedNotification(
            asylumCase, notificationsHelper, homeOfficeInstructService, Event.DECIDE_FTPA_APPLICATION, "RJ_")
        ).thenReturn("FAIL");

        PreSubmitCallbackResponse<AsylumCase> response =
            decideFtpaApplicationNotificationHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void handling_should_throw_if_event_not_applicable() {

        when(callback.getEvent()).thenReturn(Event.UNKNOWN);

        assertThatThrownBy(() -> decideFtpaApplicationNotificationHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handling_should_throw_if_not_bound_to__about_to_submit__callback_stage() {

        assertThatThrownBy(() -> decideFtpaApplicationNotificationHandler.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = decideFtpaApplicationNotificationHandler.canHandle(callbackStage, callback);

                if (event == Event.DECIDE_FTPA_APPLICATION
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

        assertThatThrownBy(() -> decideFtpaApplicationNotificationHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> decideFtpaApplicationNotificationHandler.canHandle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> decideFtpaApplicationNotificationHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> decideFtpaApplicationNotificationHandler.handle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}