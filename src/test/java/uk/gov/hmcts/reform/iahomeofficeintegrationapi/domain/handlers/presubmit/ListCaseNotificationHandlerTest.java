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
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.DECISION_HEARING_FEE_OPTION;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_HEARING_INSTRUCT_STATUS;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.WITNESS_COUNT;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.WITNESS_DETAILS;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.Hearing;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HearingInstructMessage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.MessageType;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.HearingCentre;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.WitnessDetails;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.HomeOfficeInstructService;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.DateTimeExtractor;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ListCaseNotificationHandlerTest extends AbstractNotificationsHandlerTestBase {

    @Mock
    private HomeOfficeInstructService homeOfficeInstructService;
    @Mock
    private DateTimeExtractor dateTimeExtractor;

    @Captor
    private ArgumentCaptor<HearingInstructMessage> hearingInstructMessageArgumentCaptor;

    private ListCaseNotificationHandler listCaseNotificationHandler;

    @BeforeEach
    void setUp() {

        listCaseNotificationHandler =
            new ListCaseNotificationHandler(
                homeOfficeInstructService, notificationsHelper,
                dateTimeExtractor);
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    void check_handler_returns_case_data_for_valid_input() {

        setupCase(Event.LIST_CASE);
        setupCaseData();
        setupHelperResponses();
        when(homeOfficeInstructService.sendNotification(any())).thenReturn("OK");
        setupCaseHearingDetails();

        PreSubmitCallbackResponse<AsylumCase> response =
            listCaseNotificationHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        assertTrue(response.getErrors().isEmpty());
        verify(asylumCase, times(1)).write(HOME_OFFICE_HEARING_INSTRUCT_STATUS, "OK");
        verify(homeOfficeInstructService).sendNotification(hearingInstructMessageArgumentCaptor.capture());

        final HearingInstructMessage instructMessage = hearingInstructMessageArgumentCaptor.getValue();
        assertNotificationInstructMessage(instructMessage);
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    void check_handler_returns_case_data_for_valid_input_with_default_hearing_type() {

        setupCase(Event.LIST_CASE);
        setupCaseData();
        setupHelperResponses();
        when(homeOfficeInstructService.sendNotification(any())).thenReturn("OK");
        setupCaseHearingDetails();
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> response =
            listCaseNotificationHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        assertTrue(response.getErrors().isEmpty());
        verify(asylumCase, times(1)).write(HOME_OFFICE_HEARING_INSTRUCT_STATUS, "OK");
        verify(homeOfficeInstructService).sendNotification(hearingInstructMessageArgumentCaptor.capture());

        final HearingInstructMessage instructMessage = hearingInstructMessageArgumentCaptor.getValue();
        assertNotificationInstructMessage(instructMessage);
    }

    private void setupCaseHearingDetails() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
            .thenReturn(Optional.of(HearingCentre.MANCHESTER));
        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of("2018-12-31T12:34:56"));
        when(asylumCase.read(WITNESS_COUNT, String.class)).thenReturn(Optional.of("2"));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of("ariaListingReference"));

        List<IdValue<WitnessDetails>> witnessDetails = new ArrayList<>();
        Collections.addAll(witnessDetails,
            new IdValue<>("0", new WitnessDetails("Witness 01")),
            new IdValue<>("1", new WitnessDetails("Witness 02"))
        );

        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(witnessDetails));
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class)).thenReturn(Optional.of("decisionWithHearing"));
        when(dateTimeExtractor.extractHearingDate(anyString())).thenReturn("2020-10-01");
        when(dateTimeExtractor.extractHearingTime(anyString())).thenReturn("10:10:10");
    }

    private void assertNotificationInstructMessage(HearingInstructMessage instructMessage) {
        assertThat(instructMessage.getConsumerReference()).isEqualTo(consumerReference);
        assertThat(instructMessage.getMessageHeader()).isEqualTo(messageHeader);
        assertThat(instructMessage.getHoReference()).isEqualTo(someDocumentReference);
        assertThat(instructMessage.getMessageType()).isEqualTo(MessageType.HEARING.toString());

        final Hearing hearing = instructMessage.getHearing();
        assertThat(hearing.getHearingDate()).isEqualTo("2020-10-01");
        assertThat(hearing.getHearingTime()).isEqualTo("10:10:10");
        assertThat(hearing.getHearingLocation()).isEqualTo("manchester");
        assertThat(hearing.getHmctsHearingRef()).isEqualTo("ariaListingReference");
        assertThat(hearing.getWitnessQty()).isEqualTo(2);
        assertThat(hearing.getWitnessNames()).isEqualTo("Witness 01, Witness 02");
        assertThat(hearing.getHearingType()).isEqualTo("ORAL");

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    void check_handler_returns_error_status() {

        setupCase(Event.LIST_CASE);
        setupCaseData();
        setupHelperResponses();
        when(homeOfficeInstructService.sendNotification(any(HearingInstructMessage.class)))
            .thenReturn("FAIL");

        setupCaseHearingDetails();

        PreSubmitCallbackResponse<AsylumCase> response =
            listCaseNotificationHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        assertThat(response.getErrors()).isEmpty();
        verify(asylumCase, times(1)).write(HOME_OFFICE_HEARING_INSTRUCT_STATUS, "FAIL");
        verify(homeOfficeInstructService).sendNotification(hearingInstructMessageArgumentCaptor.capture());

        final HearingInstructMessage instructMessage =
            hearingInstructMessageArgumentCaptor.getValue();
        assertNotificationInstructMessage(instructMessage);
    }

    @Test
    void handling_should_throw_if_event_not_applicable() {

        when(callback.getEvent()).thenReturn(Event.UNKNOWN);

        assertThatThrownBy(() -> listCaseNotificationHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handling_should_throw_if_not_bound_to__about_to_submit__callback_stage() {

        assertThatThrownBy(() -> listCaseNotificationHandler.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = listCaseNotificationHandler.canHandle(callbackStage, callback);

                if (event == Event.LIST_CASE
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

        assertThatThrownBy(() -> listCaseNotificationHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> listCaseNotificationHandler.canHandle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> listCaseNotificationHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> listCaseNotificationHandler.handle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_throw_error_for_case_reference_null_value() {

        setupCase(Event.LIST_CASE);

        assertThatThrownBy(() -> listCaseNotificationHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Case ID for the appeal is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }
}
