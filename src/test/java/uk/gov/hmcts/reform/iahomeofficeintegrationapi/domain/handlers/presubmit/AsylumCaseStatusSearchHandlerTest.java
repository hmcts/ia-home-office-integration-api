package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.presubmit;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.util.FileCopyUtils;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeSearchResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.HomeOfficeSearchService;

@SpringJUnitConfig
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class AsylumCaseStatusSearchHandlerTest {

    private final String someHomeOfficeReference = "some-reference";
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private HomeOfficeSearchService homeOfficeSearchService;

    @Value("classpath:home-office-sample-response.json")
    private Resource resource;

    private AsylumCaseStatusSearchHandler asylumCaseStatusSearchHandler;

    @BeforeEach
    void setUp() {
        asylumCaseStatusSearchHandler = new AsylumCaseStatusSearchHandler(homeOfficeSearchService);
    }

    @Test
    public void check_handler_returns_case_data_with_home_office_fields() throws Exception {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getState()).thenReturn(State.APPEAL_SUBMITTED);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(someHomeOfficeReference));
        when(homeOfficeSearchService.getCaseStatus(anyString())).thenReturn(getSampleResponse());

        PreSubmitCallbackResponse<AsylumCase> response =
            asylumCaseStatusSearchHandler.handle(ABOUT_TO_SUBMIT, callback);
        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        verify(asylumCase, times(1))
            .write(AsylumCaseDefinition.HO_APPELLANT_GIVEN_NAME, "Capability");
        verify(asylumCase, times(1))
            .write(AsylumCaseDefinition.HO_APPELLANT_FAMILY_NAME, "Smith");
        verify(asylumCase, times(1))
            .write(AsylumCaseDefinition.HO_APPELLANT_FULL_NAME, "Capability Smith");
        verify(asylumCase, times(1))
            .write(AsylumCaseDefinition.HO_APPELLANT_NATIONALITY, "Canada");
        verify(asylumCase, times(1))
            .write(AsylumCaseDefinition.HO_APPLICATION_DECISION, "Rejected");
        verify(asylumCase, times(1))
            .write(AsylumCaseDefinition.HO_APPLICATION_DECISION_DATE, "14/06/2020");
        verify(asylumCase, times(1))
            .write(AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS, "SUCCESS");

    }

    @Test
    public void check_handler_returns_case_data_with_errors_data() {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getState()).thenReturn(State.APPEAL_SUBMITTED);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(someHomeOfficeReference));
        when(homeOfficeSearchService.getCaseStatus(anyString())).thenReturn(null);

        assertThatThrownBy(() -> asylumCaseStatusSearchHandler.handle(ABOUT_TO_SUBMIT, callback))
            .isExactlyInstanceOf(NullPointerException.class);

    }

    @Test
    public void handler_should_throw_if_event_not_applicable() {

        when(callback.getEvent()).thenReturn(Event.UNKNOWN);

        assertThatThrownBy(() -> asylumCaseStatusSearchHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void handler_should_throw_if_not_bound_to__about_to_submit__callback_stage() {

        assertThatThrownBy(() -> asylumCaseStatusSearchHandler.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getState()).thenReturn(State.APPEAL_SUBMITTED);

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = asylumCaseStatusSearchHandler.canHandle(callbackStage, callback);

                if (event == Event.SUBMIT_APPEAL
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
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> asylumCaseStatusSearchHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> asylumCaseStatusSearchHandler.canHandle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> asylumCaseStatusSearchHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> asylumCaseStatusSearchHandler.handle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    public void should_not_allow_empty_home_office_reference() {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getState()).thenReturn(State.APPEAL_SUBMITTED);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        assertThatThrownBy(() -> asylumCaseStatusSearchHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Home office reference for the appeal is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    private HomeOfficeSearchResponse getSampleResponse() throws Exception {
        Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8);
        ObjectMapper om = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return om.readValue(FileCopyUtils.copyToString(reader), HomeOfficeSearchResponse.class);
    }

}
