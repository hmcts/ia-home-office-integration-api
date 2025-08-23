package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.presubmit;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPEAL_OUT_OF_COUNTRY;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPELLANT_DATE_OF_BIRTH;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_API_ERROR;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_APPELLANTS_LIST;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER_BEFORE_EDIT;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_SEARCH_RESPONSE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS_MESSAGE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.MATCHING_APPELLANT_DETAILS_FOUND;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event.REQUEST_HOME_OFFICE_DATA;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.util.FileCopyUtils;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.HomeOfficeDataErrorsHelper;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.HomeOfficeDataMatchHelper;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeCaseStatus;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeError;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeSearchResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.MessageHeader;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.DynamicList;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Value;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.HomeOfficeSearchService;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.HomeOfficeResponseException;

@SpringJUnitConfig
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class RequestHomeOfficeDataPreparerTest {

    private static final String PROBLEM_MESSAGE = "### There is a problem\n\n";

    private static final String HOME_OFFICE_CALL_ERROR_MESSAGE = PROBLEM_MESSAGE
            + "The service has been unable t"
            + "o retrieve the Home Office information about this appeal.\n\n"
            + "[Request the Home Office information](/case/IA/Asylum/${[CASE_REFERENCE]}/"
            + "trigger/requestHomeOfficeData) to try again. This may take a few minutes.";

    private static final String INVALID_HOME_OFFICE_REFERENCE = "The Home office does not recognise the submitted "
            + "appellant reference";

    private static HomeOfficeSearchResponse homeOfficeSearchResponse;
    private final String someHomeOfficeReference = "some-reference";
    @Mock
    private FeatureToggler featureToggler;
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
    @Mock
    private MessageHeader messageHeader;
    @Spy
    private HomeOfficeDataErrorsHelper homeOfficeDataErrorsHelper;
    @Spy
    private HomeOfficeDataMatchHelper homeOfficeDataMatchHelper;

    @org.springframework.beans.factory.annotation.Value("classpath:home-office-sample-response.json")
    private Resource resource;

    long caseId = 1234;

    private RequestHomeOfficeDataPreparer requestHomeOfficeDataPreparer;

    @BeforeEach
    void setUp() {

        requestHomeOfficeDataPreparer =
                new RequestHomeOfficeDataPreparer(
                        homeOfficeSearchService, homeOfficeDataErrorsHelper, homeOfficeDataMatchHelper, featureToggler);
    }

    @Test
    void set_error_for_general_error_sets_values_in_asylum_case() {

        homeOfficeDataErrorsHelper.setErrorMessageForErrorCode(caseId, asylumCase, null, null);
        verifyAsylumCaseFailureStatusUpdated();
    }

    @Test
    void handler_should_throw_if_event_not_applicable() {

        when(callback.getEvent()).thenReturn(Event.UNKNOWN);

        assertThatThrownBy(() -> requestHomeOfficeDataPreparer.handle(ABOUT_TO_START, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handler_should_return_error_for_out_of_country_appeals() {

        setUpCallbackStubbings();

        when(asylumCase.read(APPEAL_OUT_OF_COUNTRY, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<AsylumCase> response =
                requestHomeOfficeDataPreparer.handle(ABOUT_TO_START, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getErrors()).isNotEmpty();
        assertThat(response.getErrors()).contains("You cannot request Home Office data for an out of country appeal");
    }

    @Test
    void check_handler_returns_case_data_with_errors_data() throws Exception {

        final List<Value> values = new ArrayList<>();
        values.add(new Value("NoMatch", "No Match"));

        setUpCallbackStubbings();

        setUpAsylumCaseStubbings();

        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of(someHomeOfficeReference));
        when(homeOfficeSearchService.getCaseStatus(eq(caseId), anyString()))
                .thenThrow(new HomeOfficeResponseException("some-error"));

        PreSubmitCallbackResponse<AsylumCase> response =
                requestHomeOfficeDataPreparer.handle(ABOUT_TO_START, callback);

        DynamicList appellantsList = new DynamicList(values.get(0), values);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);

        verify(asylumCase, times(1)).write(HOME_OFFICE_APPELLANTS_LIST, appellantsList);
        verifyAsylumCaseFailureStatusUpdated();
        verify(asylumCase, times(1)).write(HOME_OFFICE_API_ERROR, INVALID_HOME_OFFICE_REFERENCE);
    }

    private void verifyAsylumCaseFailureStatusUpdated() {
        verify(asylumCase, times(1))
                .write(HOME_OFFICE_SEARCH_STATUS, "FAIL");
        verify(asylumCase, times(1))
                .write(HOME_OFFICE_SEARCH_STATUS_MESSAGE, HOME_OFFICE_CALL_ERROR_MESSAGE);
    }

    @Test
    void check_handler_returns_case_data_with_error_status_for_null_fields() throws Exception {

        setUpCallbackStubbings();

        setUpAsylumCaseStubbings();

        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of(someHomeOfficeReference));
        when(homeOfficeSearchService.getCaseStatus(eq(caseId), anyString())).thenReturn(null);

        PreSubmitCallbackResponse<AsylumCase> response =
                requestHomeOfficeDataPreparer.handle(ABOUT_TO_START, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        verifyAsylumCaseFailureStatusUpdated();
    }

    @Test
    void check_handler_returns_case_data_with_empty_fields() throws Exception {

        final List<Value> values = new ArrayList<>();
        values.add(new Value("NoMatch", "No Match"));

        setUpCallbackStubbings();

        setUpAsylumCaseStubbings();

        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of(someHomeOfficeReference));
        when(homeOfficeSearchService.getCaseStatus(eq(caseId), anyString())).thenReturn(mockResponse);

        PreSubmitCallbackResponse<AsylumCase> response =
                requestHomeOfficeDataPreparer.handle(ABOUT_TO_START, callback);

        DynamicList appellantsList = new DynamicList(values.get(0), values);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        verify(asylumCase, times(1)).write(MATCHING_APPELLANT_DETAILS_FOUND, YesOrNo.NO);
        verify(asylumCase, times(1)).write(HOME_OFFICE_APPELLANTS_LIST, appellantsList);
        verify(asylumCase, times(1)).read(HOME_OFFICE_SEARCH_RESPONSE, String.class);
        verify(asylumCase, times(1))
                .read(HOME_OFFICE_REFERENCE_NUMBER_BEFORE_EDIT, String.class);
    }

    @Test
    void handler_should_return_all_appellants_from_existing_response() throws Exception {

        final List<Value> values = new ArrayList<>();
        populateListWithUserData(values);

        setUpCallbackStubbings();
        setUpAsylumCaseStubbings();

        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of("1234-1234-5678-5678"));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER_BEFORE_EDIT, String.class))
                .thenReturn(Optional.of("1234-1234-5678-5678"));
        when(asylumCase.read(HOME_OFFICE_SEARCH_RESPONSE, String.class))
                .thenReturn(Optional.of(new ObjectMapper().writeValueAsString(getSampleResponse())));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                requestHomeOfficeDataPreparer.handle(ABOUT_TO_START, callback);

        DynamicList appellantsList = new DynamicList(values.get(0), values);

        assertThat(callbackResponse).isNotNull();
        assertThat(callbackResponse.getData()).isNotEmpty();
        assertThat(callbackResponse.getData()).isEqualTo(asylumCase);

        verify(asylumCase, times(1)).write(HOME_OFFICE_SEARCH_STATUS, "SUCCESS");
        verify(asylumCase, times(1)).write(MATCHING_APPELLANT_DETAILS_FOUND, YesOrNo.YES);
        verify(asylumCase, times(1)).write(HOME_OFFICE_APPELLANTS_LIST, appellantsList);
        verify(asylumCase, times(0)).write(HOME_OFFICE_SEARCH_RESPONSE, String.class);
        verify(homeOfficeSearchService, times(0)).getCaseStatus(caseId, someHomeOfficeReference);
    }

    @Test
    void handler_should_return_all_appellants_found_in_the_home_office_response() throws Exception {

        final List<Value> values = new ArrayList<>();
        populateListWithUserData(values);

        setUpCallbackStubbings();
        setUpAsylumCaseStubbings();

        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of("1234-1234-5678-5678"));
        when(homeOfficeSearchService.getCaseStatus(eq(caseId), anyString())).thenReturn(getSampleResponse());

        PreSubmitCallbackResponse<AsylumCase> response =
                requestHomeOfficeDataPreparer.handle(ABOUT_TO_START, callback);

        DynamicList appellantsList = new DynamicList(values.get(0), values);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        verify(asylumCase, times(1)).write(HOME_OFFICE_SEARCH_STATUS, "SUCCESS");
        verify(asylumCase, times(1)).write(MATCHING_APPELLANT_DETAILS_FOUND, YesOrNo.YES);
        verify(asylumCase, times(1)).write(HOME_OFFICE_APPELLANTS_LIST, appellantsList);
        verify(asylumCase, times(1))
                .write(HOME_OFFICE_SEARCH_RESPONSE, new ObjectMapper().writeValueAsString(getSampleResponse()));
    }

    private static void populateListWithUserData(List<Value> values) {
        Collections.addAll(values,
                new Value("John Smith", "John Smith-290268"),
                new Value("Capability Smith", "Capability Smith-210170"),
                new Value("NoMatch", "No Match"));
    }

    private void setUpAsylumCaseStubbings() {
        when(asylumCase.read(APPEAL_OUT_OF_COUNTRY, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of("1970-01-21"));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of("Stephen"));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of("Fenn"));
    }

    @Test
    void handler_should_error_if_ho_search_response_has_errors() throws Exception {

        String errorCode = "2021";
        HomeOfficeError hoError = new HomeOfficeError(errorCode, "SomeApiFailure", false);

        setUpCallbackStubbings();
        setUpAsylumCaseStubbings();

        HomeOfficeSearchResponse hoSearchResponse = new HomeOfficeSearchResponse(
                messageHeader, "someMsgType", Arrays.asList(caseStatus), hoError);

        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of(someHomeOfficeReference));
        when(homeOfficeSearchService.getCaseStatus(eq(caseId), anyString()))
                .thenReturn(hoSearchResponse);
        PreSubmitCallbackResponse<AsylumCase> response =
                requestHomeOfficeDataPreparer.handle(ABOUT_TO_START, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        verify(homeOfficeDataErrorsHelper, times(1))
                .setErrorMessageForErrorCode(
                        caseId, asylumCase, errorCode, "Error code: 2021, message: SomeApiFailure");
        verifyAsylumCaseFailureStatusUpdated();
    }

    private void setUpCallbackStubbings() {
        when(featureToggler.getValue("home-office-uan-feature", false)).thenReturn(true);
        when(callback.getEvent()).thenReturn(REQUEST_HOME_OFFICE_DATA);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(caseId);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void handler_should_throw_if_not_bound_to__about_to_submit__callback_stage() {

        assertThatThrownBy(() -> requestHomeOfficeDataPreparer.handle(ABOUT_TO_START, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        when(featureToggler.getValue("home-office-uan-feature", false)).thenReturn(true);
        for (Event event : Event.values()) {

            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = requestHomeOfficeDataPreparer.canHandle(callbackStage, callback);

                if (callbackStage == ABOUT_TO_START
                        && (callback.getEvent() == REQUEST_HOME_OFFICE_DATA)
                ) {
                    Assertions.assertTrue(canHandle);
                } else {
                    Assertions.assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> requestHomeOfficeDataPreparer.canHandle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> requestHomeOfficeDataPreparer.canHandle(ABOUT_TO_START, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> requestHomeOfficeDataPreparer.handle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> requestHomeOfficeDataPreparer.handle(ABOUT_TO_START, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
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
}
