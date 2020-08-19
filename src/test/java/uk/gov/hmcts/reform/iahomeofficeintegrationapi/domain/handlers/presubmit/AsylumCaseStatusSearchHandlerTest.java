package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.presubmit;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_CASE_STATUS_DATA;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS_MESSAGE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event.MARK_APPEAL_PAID;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event.PAY_AND_SUBMIT_APPEAL;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event.REQUEST_HOME_OFFICE_DATA;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event.SUBMIT_APPEAL;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeCaseStatus;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeError;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeMetadata;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeSearchResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.HomeOfficeSearchService;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.HomeOfficeResponseException;

@SpringJUnitConfig
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class AsylumCaseStatusSearchHandlerTest {

    private static final String HOME_OFFICE_ERROR_MESSAGE = "The service has been "
        + "unable to retrieve the Home Office information about this appeal.";

    private static HomeOfficeSearchResponse homeOfficeSearchResponse;
    private static HomeOfficeSearchResponse homeOfficeNullFieldResponse;
    private final String someHomeOfficeReference = "some-reference";
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private HomeOfficeSearchService homeOfficeSearchService;
    @Mock
    private HomeOfficeCaseStatus caseStatus;
    @Mock
    private HomeOfficeSearchResponse mockResponse;

    @Value("classpath:home-office-sample-response.json")
    private Resource resource;
    @Value("classpath:home-office-null-field-response.json")
    private Resource resourceNullField;

    private AsylumCaseStatusSearchHandler asylumCaseStatusSearchHandler;

    @BeforeEach
    void setUp() {
        asylumCaseStatusSearchHandler = new AsylumCaseStatusSearchHandler(homeOfficeSearchService);
    }

    @Test
    void check_handler_returns_case_data_with_home_office_fields() throws Exception {

        when(callback.getEvent()).thenReturn(SUBMIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(someHomeOfficeReference));
        when(asylumCase.read(HOME_OFFICE_CASE_STATUS_DATA, HomeOfficeCaseStatus.class))
            .thenReturn(Optional.of(caseStatus));
        when(homeOfficeSearchService.getCaseStatus(anyString())).thenReturn(getSampleResponse());

        PreSubmitCallbackResponse<AsylumCase> response =
            asylumCaseStatusSearchHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        verify(asylumCase, times(1))
            .write(AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS, "SUCCESS");
        assertTrue(asylumCase.read(HOME_OFFICE_CASE_STATUS_DATA, HomeOfficeCaseStatus.class).isPresent());

    }

    @Test
    void check_handler_returns_case_data_with_errors_data() throws Exception {

        when(callback.getEvent()).thenReturn(SUBMIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(someHomeOfficeReference));
        when(homeOfficeSearchService.getCaseStatus(anyString()))
            .thenThrow(new HomeOfficeResponseException("some-error"));

        PreSubmitCallbackResponse<AsylumCase> response =
            asylumCaseStatusSearchHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        verify(asylumCase, times(1))
            .write(AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS, "FAIL");
        verify(asylumCase, times(1))
            .write(HOME_OFFICE_SEARCH_STATUS_MESSAGE, HOME_OFFICE_ERROR_MESSAGE);

    }

    @Test
    void check_handler_returns_case_data_with_error_status_for_null_fields() throws Exception {

        when(callback.getEvent()).thenReturn(REQUEST_HOME_OFFICE_DATA);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(someHomeOfficeReference));
        when(homeOfficeSearchService.getCaseStatus(anyString())).thenReturn(null);

        PreSubmitCallbackResponse<AsylumCase> response =
            asylumCaseStatusSearchHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        verify(asylumCase, times(1))
            .write(AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS, "FAIL");
        verify(asylumCase, times(1))
            .write(HOME_OFFICE_SEARCH_STATUS_MESSAGE, HOME_OFFICE_ERROR_MESSAGE);

    }

    @Test
    void check_handler_validates_person_null_value_from_home_office_data() throws Exception {

        when(callback.getEvent()).thenReturn(PAY_AND_SUBMIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(someHomeOfficeReference));
        when(homeOfficeSearchService.getCaseStatus(anyString())).thenReturn(getNullFieldResponse());

        PreSubmitCallbackResponse<AsylumCase> response =
            asylumCaseStatusSearchHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        verify(asylumCase, times(1))
            .write(AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS, "FAIL");
        verify(asylumCase, times(1))
            .write(HOME_OFFICE_SEARCH_STATUS_MESSAGE, HOME_OFFICE_ERROR_MESSAGE);

    }

    @Test
    void check_handler_validates_error_detail_from_home_office_data_returns_fail() throws Exception {

        when(callback.getEvent()).thenReturn(MARK_APPEAL_PAID);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(someHomeOfficeReference));
        when(homeOfficeSearchService.getCaseStatus(anyString())).thenReturn(mockResponse);
        when(mockResponse.getErrorDetail()).thenReturn(new HomeOfficeError(
            "1010",
            "UAN format is invalid",
            true
        ));

        PreSubmitCallbackResponse<AsylumCase> response =
            asylumCaseStatusSearchHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        verify(asylumCase, times(1))
            .write(AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS, "FAIL");
        verify(asylumCase, times(1))
            .write(HOME_OFFICE_SEARCH_STATUS_MESSAGE, HOME_OFFICE_ERROR_MESSAGE);

    }

    @Test
    void handler_should_throw_if_event_not_applicable() {

        when(callback.getEvent()).thenReturn(Event.UNKNOWN);

        assertThatThrownBy(() -> asylumCaseStatusSearchHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handler_should_throw_if_not_bound_to__about_to_submit__callback_stage() {

        assertThatThrownBy(() -> asylumCaseStatusSearchHandler.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = asylumCaseStatusSearchHandler.canHandle(callbackStage, callback);

                if (callbackStage == ABOUT_TO_SUBMIT
                    && (callback.getEvent() == SUBMIT_APPEAL
                    || callback.getEvent() == PAY_AND_SUBMIT_APPEAL
                    || callback.getEvent() == MARK_APPEAL_PAID
                    || callback.getEvent() == REQUEST_HOME_OFFICE_DATA)
                ) {
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
    void should_not_allow_empty_home_office_reference() {

        when(callback.getEvent()).thenReturn(SUBMIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        assertThatThrownBy(() -> asylumCaseStatusSearchHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Home office reference for the appeal is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void reject_reasons_returned_as_one_string_formatted() throws Exception {

        String rejectReason = asylumCaseStatusSearchHandler.getRejectionReasonString(
            getSampleResponse().getStatus().get(1).getApplicationStatus().getRejectionReasons());

        assertNotNull(rejectReason);
        assertEquals("Application not completed properly" + "<br />" + "Application not entered properly",
            rejectReason);

    }

    @Test
    void reject_reasons_returned_as_empty_string_for_null_or_empty_value() {

        String rejectReason = asylumCaseStatusSearchHandler.getRejectionReasonString(null);
        assertEquals("", rejectReason);
        rejectReason = asylumCaseStatusSearchHandler.getRejectionReasonString(new ArrayList<>());
        assertEquals("", rejectReason);
    }

    @Test
    void main_applicant_status_returned_from_valid_set_of_status() throws Exception {

        Optional<HomeOfficeCaseStatus> searchStatus = asylumCaseStatusSearchHandler.selectMainApplicant(
            getSampleResponse().getStatus());

        assertNotNull(searchStatus);
        assertTrue(searchStatus.isPresent());
    }

    @Test
    void main_applicant_status_returned_empty_from_invalid_set_of_status() throws Exception {
        List<HomeOfficeCaseStatus> invalidList = new ArrayList<>();
        invalidList.add(getSampleResponse().getStatus().get(0));
        Optional<HomeOfficeCaseStatus> searchStatus = asylumCaseStatusSearchHandler.selectMainApplicant(invalidList);

        assertNotNull(searchStatus);
        assertFalse(searchStatus.isPresent());
    }

    @Test
    void main_applicant_status_returned_empty_from_null_or_empty_list() {
        Optional<HomeOfficeCaseStatus> searchStatus = asylumCaseStatusSearchHandler.selectMainApplicant(null);
        assertFalse(searchStatus.isPresent());
        searchStatus = asylumCaseStatusSearchHandler.selectMainApplicant(Collections.EMPTY_LIST);
        assertFalse(searchStatus.isPresent());
    }

    @Test
    void metadata_returned_from_valid_set_of_values() throws Exception {

        Optional<HomeOfficeMetadata> metadata = asylumCaseStatusSearchHandler.selectMetadata(
            getSampleResponse().getStatus().get(1).getApplicationStatus().getHomeOfficeMetadata());

        assertNotNull(metadata);
        assertTrue(metadata.isPresent());
    }

    @Test
    void metadata_returned_empty_from_invalid_set_of_values() throws Exception {
        Optional<HomeOfficeMetadata> metadata = asylumCaseStatusSearchHandler.selectMetadata(
            getSampleResponse().getStatus().get(0).getApplicationStatus().getHomeOfficeMetadata());

        assertNotNull(metadata);
        assertFalse(metadata.isPresent());
    }

    @Test
    void metadata_returned_empty_from_null_or_empty_list() {
        Optional<HomeOfficeMetadata> metadata = asylumCaseStatusSearchHandler.selectMetadata(null);
        assertFalse(metadata.isPresent());
        metadata = asylumCaseStatusSearchHandler.selectMetadata(Collections.EMPTY_LIST);
        assertFalse(metadata.isPresent());
    }

    private HomeOfficeSearchResponse getSampleResponse() throws Exception {
        if (homeOfficeSearchResponse == null) {
            Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8);
            ObjectMapper om = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            homeOfficeSearchResponse = om.readValue(FileCopyUtils.copyToString(reader), HomeOfficeSearchResponse.class);
        }
        return homeOfficeSearchResponse;
    }

    private HomeOfficeSearchResponse getNullFieldResponse() throws Exception {
        if (homeOfficeNullFieldResponse == null) {
            Reader reader = new InputStreamReader(resourceNullField.getInputStream(), UTF_8);
            ObjectMapper om = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            homeOfficeNullFieldResponse = om.readValue(
                FileCopyUtils.copyToString(reader), HomeOfficeSearchResponse.class);
        }
        return homeOfficeNullFieldResponse;
    }

}
