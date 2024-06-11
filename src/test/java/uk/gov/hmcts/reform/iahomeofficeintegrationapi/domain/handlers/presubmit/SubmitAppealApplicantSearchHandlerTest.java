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
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPELLANT_DATE_OF_BIRTH;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_CASE_STATUS_DATA;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_SEARCH_RESPONSE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS_MESSAGE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event.MARK_APPEAL_PAID;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event.PAY_AND_SUBMIT_APPEAL;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event.SUBMIT_APPEAL;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.util.FileCopyUtils;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.HomeOfficeDataErrorsHelper;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.HomeOfficeDataMatchHelper;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeCaseStatus;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeError;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeMetadata;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeSearchResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.Person;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.HomeOfficeSearchService;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.HomeOfficeResponseException;

@SpringJUnitConfig
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class SubmitAppealApplicantSearchHandlerTest {

    private static final String HOME_OFFICE_CALL_ERROR_MESSAGE = """
        ### There is a problem
    
        The service has been unable to retrieve the Home Office information about this appeal.
    
        [Request the Home Office information](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/requestHomeOfficeData)\
         to try again. This may take a few minutes.""";

    private static final String HOME_OFFICE_INVALID_REFERENCE_ERROR_MESSAGE = """
        ### There is a problem
    
        The appellant entered the Home Office reference number incorrectly. You can contact the appellant to check the\
         reference number if you need this information to validate the appeal""";

    private static final String HOME_OFFICE_REFERENCE_NOT_FOUND_ERROR_MESSAGE = """
        ### There is a problem
    
        The appellant’s Home Office reference number could not be found. You can contact the Home Office to check the\
         reference if you need this information to validate the appeal""";

    private static final String HOME_OFFICE_APPELLANT_NOT_FOUND_ERROR_MESSAGE = """
        ### There is a problem
    
        The service has been unable to retrieve the Home Office information about this appeal because the Home Office\
         reference number does not have any matching appellant data in the system. You can contact the Home Office if\
         you need more information to validate the appeal.""";

    private static final String HOME_OFFICE_WRONG_APPLICANT_NOT_FOUND_ERROR_MESSAGE =
            """
            **Note:** The service has been unable to retrieve the Home Office information about this appeal because\
             the Home Office Reference/Case ID, date of birth or name submitted by the appellant do not match the\
             details stored by the Home Office
            ## Do this next
            - Contact the Home Office to get the correct details
            - Use [Edit appeal](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/editAppealAfterSubmit) to update the\
             details as required
            - [Request Home Office data](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/requestHomeOfficeData) to\
             match the appellant details with the Home Office details""";

    private static final String HOME_OFFICE_MULTIPLE_APPELLANTS_ERROR_MESSAGE =
            """
            The Home Office data has returned more than one appellant for this appeal.\s
            ## Do this next
             You need to [request home office data](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/requestHomeOfficeData)\
             to select the correct appellant for this appeal.""";
    private static final String HOME_OFFICE_UAN_FEATURE = "home-office-uan-feature";
    private static final String FAIL = "FAIL";
    private static final String SUCCESS = "SUCCESS";
    private static final String SAMPLE_ERROR = "some-error";
    private static final String NOT_FOUND_ERROR = "Not found";
    private static final String SAMPLE_FIRST_NAME = "Capability";
    private static final String SAMPLE_LAST_NAME = "Brown";
    private static final String SECOND_SAMPLE_FIRST_NAME = "Stephen";
    private static final String SECOND_SAMPLE_LAST_NAME = "Fenn";
    private static HomeOfficeSearchResponse homeOfficeSearchResponse;
    private static HomeOfficeSearchResponse homeOfficeNullFieldResponse;
    private static HomeOfficeSearchResponse homeOfficeMultipleApplicantsResponse;
    private static final String someHomeOfficeReference = "some-reference";
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
    @Spy
    private HomeOfficeDataErrorsHelper homeOfficeDataErrorsHelper;
    @Spy
    private HomeOfficeDataMatchHelper homeOfficeDataMatchHelper;

    @Value("classpath:home-office-sample-response.json")
    private Resource resource;
    @Value("classpath:home-office-null-field-response.json")
    private Resource resourceNullField;
    @Value("classpath:home-office-multiple-applicants-response.json")
    private Resource resourceMultipleApplicants;

    long caseId = 1234;

    private SubmitAppealApplicantSearchHandler submitAppealApplicantSearchHandler;

    @BeforeEach
    void setUp() {
        submitAppealApplicantSearchHandler =
                new SubmitAppealApplicantSearchHandler(
                        homeOfficeSearchService, homeOfficeDataMatchHelper, homeOfficeDataErrorsHelper, featureToggler);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = { "SUBMIT_APPEAL", "PAY_AND_SUBMIT_APPEAL", "MARK_APPEAL_PAID" })
    void check_handler_returns_case_data_with_home_office_fields() throws Exception {

        final String jsonStr = new ObjectMapper().writeValueAsString(getSampleResponse());

        setUpCallbackStubbings();
        setUpAppellantDetailStubbings();

        when(asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(someHomeOfficeReference));
        when(asylumCase.read(HOME_OFFICE_CASE_STATUS_DATA, HomeOfficeCaseStatus.class))
            .thenReturn(Optional.of(caseStatus));
        when(homeOfficeSearchService.getCaseStatus(eq(caseId), anyString())).thenReturn(getSampleResponse());

        PreSubmitCallbackResponse<AsylumCase> response =
            submitAppealApplicantSearchHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertResponseDataPopulated(response);
        verify(asylumCase, times(1))
            .write(AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS, SUCCESS);
        Assertions.assertTrue(asylumCase.read(HOME_OFFICE_CASE_STATUS_DATA, HomeOfficeCaseStatus.class).isPresent());
        verify(asylumCase, times(1)).write(HOME_OFFICE_SEARCH_RESPONSE, jsonStr);
    }

    @Test
    void check_handler_returns_case_data_with_errors_data() throws Exception {

        setUpCallbackStubbings();
        when(asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(someHomeOfficeReference));
        when(homeOfficeSearchService.getCaseStatus(eq(caseId), anyString()))
            .thenThrow(new HomeOfficeResponseException(SAMPLE_ERROR));
        setUpAppellantDetailStubbings();

        PreSubmitCallbackResponse<AsylumCase> response =
            submitAppealApplicantSearchHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertResponseDataPopulated(response);
        verifyFailedSearchStatus();
    }

    @Test
    void handler_should_error_for_multiple_applicants_present_on_the_give_uan() throws Exception {

        setUpCallbackStubbings();
        setUpMatchableAppellantStubbings();

        when(asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of(someHomeOfficeReference));
        when(homeOfficeSearchService.getCaseStatus(eq(caseId), anyString()))
                .thenReturn(getMultipleApplicantsResponse());
        String jsonStr = new ObjectMapper().writeValueAsString(getMultipleApplicantsResponse());

        PreSubmitCallbackResponse<AsylumCase> response =
                submitAppealApplicantSearchHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertResponseDataPopulated(response);
        verifyMultipleAppellantsMatched(jsonStr);
    }

    @Test
    void handler_should_error_for_no_match_applicant() throws Exception {

        setUpCallbackStubbings();
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(SAMPLE_FIRST_NAME));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(SAMPLE_LAST_NAME));
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of("1981-10-12"));

        when(asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of(someHomeOfficeReference));
        when(homeOfficeSearchService.getCaseStatus(eq(caseId), anyString()))
                .thenReturn(getMultipleApplicantsResponse());

        PreSubmitCallbackResponse<AsylumCase> response =
                submitAppealApplicantSearchHandler.handle(ABOUT_TO_SUBMIT, callback);
        String jsonStr = new ObjectMapper().writeValueAsString(getMultipleApplicantsResponse());

        assertResponseDataPopulated(response);
        verify(asylumCase, times(1))
                .write(AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS, FAIL);
        verify(asylumCase, times(0)).write(HOME_OFFICE_SEARCH_RESPONSE, jsonStr);
        verify(asylumCase, times(1))
                .write(HOME_OFFICE_SEARCH_STATUS_MESSAGE, HOME_OFFICE_WRONG_APPLICANT_NOT_FOUND_ERROR_MESSAGE);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = { "SUBMIT_APPEAL", "PAY_AND_SUBMIT_APPEAL", "MARK_APPEAL_PAID" })
    void handler_should_write_ho_response_into_case_data_if_any_applicants_details_matches() throws Exception {



        setUpCallbackStubbings();

        setUpMatchableAppellantStubbings();

        when(asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of("1234-1111-5678-1111"));
        when(homeOfficeSearchService.getCaseStatus(eq(caseId), anyString()))
                .thenReturn(getMultipleApplicantsResponse());

        PreSubmitCallbackResponse<AsylumCase> response =
                submitAppealApplicantSearchHandler.handle(ABOUT_TO_SUBMIT, callback);
        String jsonStr = new ObjectMapper().writeValueAsString(getMultipleApplicantsResponse());

        assertResponseDataPopulated(response);
        verifyMultipleAppellantsMatched(jsonStr);

    }

    @Test
    void check_get_formatted_decision_date_returns_date_when_date_is_given() {

        LocalDate formattedDecisionDate = submitAppealApplicantSearchHandler.getFormattedDecisionDate("1998-01-30");

        assertThat(formattedDecisionDate).isNotNull();
        Assertions.assertEquals(formattedDecisionDate, LocalDate.parse("1998-01-30"));

    }

    @Test
    void check_get_formatted_decision_date_returns_date_when_date_time_is_geven() {

        LocalDate formattedDecisionDate =
                submitAppealApplicantSearchHandler.getFormattedDecisionDate("2003-03-28T18:04:52Z");

        assertThat(formattedDecisionDate).isNotNull();
        Assertions.assertEquals(formattedDecisionDate, LocalDate.parse("2003-03-28"));

    }

    @Test
    void check_handler_returns_case_data_with_error_status_for_null_fields() throws Exception {

        setUpCallbackStubbings();
        when(asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(someHomeOfficeReference));
        when(homeOfficeSearchService.getCaseStatus(eq(caseId), anyString())).thenReturn(null);

        setUpAppellantDetailStubbings();

        PreSubmitCallbackResponse<AsylumCase> response =
            submitAppealApplicantSearchHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertResponseDataPopulated(response);
        verifyFailedSearchStatus();
    }

    private void verifyFailedSearchStatus() {
        verify(asylumCase, times(1))
            .write(AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS, FAIL);
        verify(asylumCase, times(1))
            .write(HOME_OFFICE_SEARCH_STATUS_MESSAGE, HOME_OFFICE_CALL_ERROR_MESSAGE);
    }

    @Test
    void handle_should_return_error_for_invalid_home_office_reference() {

        when(featureToggler.getValue(HOME_OFFICE_UAN_FEATURE, false)).thenReturn(true);
        when(callback.getEvent()).thenReturn(PAY_AND_SUBMIT_APPEAL);
        setUpCaseDetailsStubbings();
        setUpAppellantDetailStubbings();
        when(asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of("1234-1234-1234-1234-1234-1234-1234-1234-1234-1234"));

        PreSubmitCallbackResponse<AsylumCase> response =
            submitAppealApplicantSearchHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertResponseDataPopulated(response);

        verify(asylumCase, times(1))
            .write(AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS, FAIL);
        verify(asylumCase, times(1))
            .write(AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS_MESSAGE, HOME_OFFICE_INVALID_REFERENCE_ERROR_MESSAGE);
    }

    @Test
    void check_handler_validates_person_null_value_from_home_office_data() throws Exception {

        when(featureToggler.getValue(HOME_OFFICE_UAN_FEATURE, false)).thenReturn(true);
        when(callback.getEvent()).thenReturn(PAY_AND_SUBMIT_APPEAL);
        setUpCaseDetailsStubbings();
        when(asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(someHomeOfficeReference));
        when(homeOfficeSearchService.getCaseStatus(eq(caseId), anyString())).thenReturn(getNullFieldResponse());

        setUpAppellantDetailStubbings();

        PreSubmitCallbackResponse<AsylumCase> response =
            submitAppealApplicantSearchHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertResponseDataPopulated(response);
    }

    @Test
    void check_handler_validates_error_detail_from_home_office_data_returns_fail() throws Exception {

        when(featureToggler.getValue(HOME_OFFICE_UAN_FEATURE, false)).thenReturn(true);
        when(callback.getEvent()).thenReturn(MARK_APPEAL_PAID);
        setUpCaseDetailsStubbings();
        when(asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(someHomeOfficeReference));
        when(homeOfficeSearchService.getCaseStatus(eq(caseId), anyString())).thenReturn(mockResponse);
        when(mockResponse.getErrorDetail()).thenReturn(new HomeOfficeError(
            "1020",
            "No document found",
            true
        ));

        setUpAppellantDetailStubbings();

        PreSubmitCallbackResponse<AsylumCase> response =
            submitAppealApplicantSearchHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertResponseDataPopulated(response);
        verify(asylumCase, times(1))
            .write(AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS, FAIL);
        verify(asylumCase, times(1))
            .write(HOME_OFFICE_SEARCH_STATUS_MESSAGE, HOME_OFFICE_REFERENCE_NOT_FOUND_ERROR_MESSAGE);

    }

    @Test
    void check_handler_validates_no_appellant_error_from_home_office_data_returns_fail() throws Exception {

        setUpCallbackStubbings();
        when(asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(someHomeOfficeReference));
        when(homeOfficeSearchService.getCaseStatus(eq(caseId), anyString())).thenReturn(mockResponse);
        when(mockResponse.getErrorDetail()).thenReturn(new HomeOfficeError(
            "1030",
            "No service details",
            true
        ));

        setUpAppellantDetailStubbings();

        PreSubmitCallbackResponse<AsylumCase> response =
            submitAppealApplicantSearchHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertResponseDataPopulated(response);
        verify(asylumCase, times(1))
            .write(AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS, FAIL);
        verify(asylumCase, times(1))
            .write(HOME_OFFICE_SEARCH_STATUS_MESSAGE, HOME_OFFICE_APPELLANT_NOT_FOUND_ERROR_MESSAGE);

    }

    @Test
    void check_handler_validates_ho_not_found_from_home_office_data_returns_fail() throws Exception {

        when(featureToggler.getValue(HOME_OFFICE_UAN_FEATURE, false)).thenReturn(true);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        setUpCaseDetailsStubbings();
        when(asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(someHomeOfficeReference));
        when(homeOfficeSearchService.getCaseStatus(eq(caseId), anyString())).thenReturn(mockResponse);
        when(mockResponse.getErrorDetail()).thenReturn(new HomeOfficeError(
            "1060",
            "UAN format is invalid",
            true
        ));

        setUpAppellantDetailStubbings();

        PreSubmitCallbackResponse<AsylumCase> response =
            submitAppealApplicantSearchHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertResponseDataPopulated(response);
        verify(asylumCase, times(1))
            .write(AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS, FAIL);
        verify(asylumCase, times(1))
            .write(HOME_OFFICE_SEARCH_STATUS_MESSAGE, HOME_OFFICE_INVALID_REFERENCE_ERROR_MESSAGE);

    }

    @Test
    void check_handler_validates_main_applicant_not_found_from_home_office_data_returns_fail() throws Exception {

        when(featureToggler.getValue(HOME_OFFICE_UAN_FEATURE, false)).thenReturn(true);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        setUpCaseDetailsStubbings();
        when(asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(someHomeOfficeReference));
        when(homeOfficeSearchService.getCaseStatus(eq(caseId), anyString())).thenReturn(mockResponse);

        setUpAppellantDetailStubbings();

        PreSubmitCallbackResponse<AsylumCase> response =
            submitAppealApplicantSearchHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertResponseDataPopulated(response);
        verify(asylumCase, times(1))
            .write(AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS, FAIL);
    }

    @Test
    void handle_should_return_failure_for_null_ho_response() throws Exception {

        when(featureToggler.getValue(HOME_OFFICE_UAN_FEATURE, false)).thenReturn(true);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        setUpCaseDetailsStubbings();
        when(asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of(someHomeOfficeReference));
        when(homeOfficeSearchService.getCaseStatus(eq(caseId), anyString())).thenReturn(null);

        setUpAppellantDetailStubbings();

        PreSubmitCallbackResponse<AsylumCase> response =
                submitAppealApplicantSearchHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        verifyFailedSearchStatus();
    }

    @Test
    void handler_should_throw_if_event_not_applicable() {

        when(callback.getEvent()).thenReturn(Event.UNKNOWN);

        assertThatThrownBy(() -> submitAppealApplicantSearchHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handler_should_throw_if_not_bound_to__about_to_submit__callback_stage() {

        assertThatThrownBy(() -> submitAppealApplicantSearchHandler.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback_uan_feature_on() {

        when(featureToggler.getValue(HOME_OFFICE_UAN_FEATURE, false)).thenReturn(true);
        for (Event event : Event.values()) {

            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = submitAppealApplicantSearchHandler.canHandle(callbackStage, callback);

                if (callbackStage == ABOUT_TO_SUBMIT
                    && (callback.getEvent() == SUBMIT_APPEAL
                    || callback.getEvent() == PAY_AND_SUBMIT_APPEAL
                    || callback.getEvent() == MARK_APPEAL_PAID)
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

        assertThatThrownBy(() -> submitAppealApplicantSearchHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> submitAppealApplicantSearchHandler.canHandle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> submitAppealApplicantSearchHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> submitAppealApplicantSearchHandler.handle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_not_allow_empty_home_office_reference() {

        setUpCallbackStubbings();

        assertThatThrownBy(() -> submitAppealApplicantSearchHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Home office reference for the appeal is not present, caseId: " + caseId)
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void reject_reasons_returned_as_one_string_formatted() throws Exception {

        String rejectReason = submitAppealApplicantSearchHandler.getRejectionReasonString(
            getSampleResponse().getStatus().get(1).getApplicationStatus().getRejectionReasons());

        Assertions.assertNotNull(rejectReason);
        Assertions.assertEquals("Application not completed properly" + "<br />" + "Application not entered properly", rejectReason);

    }

    @Test
    void reject_reasons_returned_as_empty_string_for_null_or_empty_value() {

        String rejectReason = submitAppealApplicantSearchHandler.getRejectionReasonString(null);
        Assertions.assertEquals("", rejectReason);
        rejectReason = submitAppealApplicantSearchHandler.getRejectionReasonString(new ArrayList<>());
        Assertions.assertEquals("", rejectReason);
    }

    @Test
    void main_applicant_status_returned_from_valid_set_of_status() throws Exception {

        Person appellant = Person.PersonBuilder.person().withFamilyName(SECOND_SAMPLE_LAST_NAME).withGivenName(SECOND_SAMPLE_FIRST_NAME).build();

        List<HomeOfficeCaseStatus> searchStatus = submitAppealApplicantSearchHandler.selectMainApplicant(
            caseId,
            getSampleResponse().getStatus(),
            appellant,
            "1970-01-21"
        );

        Person person = searchStatus.get(0).getPerson();

        Assertions.assertNotNull(searchStatus);
        Assertions.assertFalse(searchStatus.isEmpty());
        assertThat(person.getFamilyName()).isEqualTo(SAMPLE_LAST_NAME);
        assertThat(person.getGivenName()).isEqualTo(SAMPLE_FIRST_NAME);
    }

    @Test
    void should_match_applicant_details_by_given_and_family_names() throws Exception {

        Person appellant = Person.PersonBuilder.person().withFamilyName(SAMPLE_LAST_NAME).withGivenName(SAMPLE_FIRST_NAME).build();

        List<HomeOfficeCaseStatus> searchStatus = submitAppealApplicantSearchHandler.selectMainApplicant(
            caseId,
            getSampleResponse().getStatus(),
            appellant,
            "1976-11-21"
        );

        Person person = searchStatus.stream().filter(p ->
                p.getPerson().getFamilyName().equalsIgnoreCase(appellant.getFamilyName())
                        && p.getPerson().getGivenName().equalsIgnoreCase(appellant.getGivenName()))
                .findAny()
                .get()
                .getPerson();

        Assertions.assertNotNull(searchStatus);
        Assertions.assertFalse(searchStatus.isEmpty());
        assertThat(person.getFamilyName()).isEqualTo(SAMPLE_LAST_NAME);
        assertThat(person.getGivenName()).isEqualTo(SAMPLE_FIRST_NAME);

    }

    @Test
    void main_applicant_status_returned_empty_from_invalid_set_of_status() throws Exception {

        Person appellant = Person.PersonBuilder.person().withFamilyName(SECOND_SAMPLE_LAST_NAME).withGivenName(SECOND_SAMPLE_FIRST_NAME).build();

        List<HomeOfficeCaseStatus> invalidList = new ArrayList<>();
        invalidList.add(getSampleResponse().getStatus().get(0));
        List<HomeOfficeCaseStatus> searchStatus =
            submitAppealApplicantSearchHandler.selectMainApplicant(
                caseId,
                invalidList,
                appellant,
                "1980-11-11"
            );

        Assertions.assertNotNull(searchStatus);
        Assertions.assertTrue(searchStatus.isEmpty());
    }

    @Test
    void main_applicant_status_returned_empty_from_null_or_empty_list() {

        Person appellant = Person.PersonBuilder.person().withFamilyName(SECOND_SAMPLE_LAST_NAME).withGivenName(SECOND_SAMPLE_FIRST_NAME).build();

        List<HomeOfficeCaseStatus> searchStatus =
            submitAppealApplicantSearchHandler.selectMainApplicant(
                caseId,
                null,
                appellant,
                "1980-11-11"
            );
        assertThat(searchStatus).isEmpty();
        searchStatus = submitAppealApplicantSearchHandler.selectMainApplicant(
            caseId,
            Collections.EMPTY_LIST,
            appellant,
            "1980-11-11"
        );
        assertThat(searchStatus).isEmpty();
    }

    @Test
    void metadata_returned_from_valid_set_of_values() throws Exception {

        Optional<HomeOfficeMetadata> metadata = submitAppealApplicantSearchHandler.selectMetadata(
            caseId,
            getSampleResponse().getStatus().get(1).getApplicationStatus().getHomeOfficeMetadata()
        );

        Assertions.assertNotNull(metadata);
        Assertions.assertTrue(metadata.isPresent());
    }

    @Test
    void metadata_returned_empty_from_invalid_set_of_values() throws Exception {
        Optional<HomeOfficeMetadata> metadata = submitAppealApplicantSearchHandler.selectMetadata(
            caseId,
            getSampleResponse().getStatus().get(0).getApplicationStatus().getHomeOfficeMetadata()
        );

        Assertions.assertNotNull(metadata);
        Assertions.assertFalse(metadata.isPresent());
    }

    @Test
    void metadata_returned_empty_from_null_or_empty_list() {
        Optional<HomeOfficeMetadata> metadata = submitAppealApplicantSearchHandler.selectMetadata(caseId,null);
        Assertions.assertFalse(metadata.isPresent());
        metadata = submitAppealApplicantSearchHandler.selectMetadata(caseId, Collections.EMPTY_LIST);
        Assertions.assertFalse(metadata.isPresent());
    }

    @Test
    void set_error_for_ho_reference_not_found_sets_values_in_asylum_case() {

        homeOfficeDataErrorsHelper.setErrorMessageForErrorCode(caseId, asylumCase, "1020", NOT_FOUND_ERROR);
        verify(asylumCase, times(1))
            .write(AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS, FAIL);
        verify(asylumCase, times(1))
            .write(HOME_OFFICE_SEARCH_STATUS_MESSAGE, HOME_OFFICE_REFERENCE_NOT_FOUND_ERROR_MESSAGE);
    }

    @Test
    void set_error_for_empty_error_code() {

        homeOfficeDataErrorsHelper.setErrorMessageForErrorCode(caseId, asylumCase, "", NOT_FOUND_ERROR);
        verifyFailedSearchStatus();
    }

    @Test
    void set_error_for_ho_appellant_not_found_sets_values_in_asylum_case() {

        homeOfficeDataErrorsHelper.setErrorMessageForErrorCode(caseId, asylumCase, "1010", NOT_FOUND_ERROR);
        verify(asylumCase, times(1))
            .write(AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS, FAIL);
        verify(asylumCase, times(1))
            .write(HOME_OFFICE_SEARCH_STATUS_MESSAGE, HOME_OFFICE_APPELLANT_NOT_FOUND_ERROR_MESSAGE);

    }

    @Test
    void set_error_for_invalid_format_sets_values_in_asylum_case() {

        homeOfficeDataErrorsHelper.setErrorMessageForErrorCode(caseId, asylumCase, "1060", "Format error");
        verify(asylumCase, times(1))
            .write(AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS, FAIL);
        verify(asylumCase, times(1))
            .write(HOME_OFFICE_SEARCH_STATUS_MESSAGE, HOME_OFFICE_INVALID_REFERENCE_ERROR_MESSAGE);

    }

    @Test
    void set_error_for_general_error_sets_values_in_asylum_case() {

        homeOfficeDataErrorsHelper.setErrorMessageForErrorCode(caseId, asylumCase, null, null);
        verifyFailedSearchStatus();

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

    private HomeOfficeSearchResponse getMultipleApplicantsResponse() throws Exception {
        if (homeOfficeMultipleApplicantsResponse == null) {
            Reader reader = new InputStreamReader(resourceMultipleApplicants.getInputStream(), UTF_8);
            ObjectMapper om = new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            homeOfficeMultipleApplicantsResponse = om.readValue(
                    FileCopyUtils.copyToString(reader), HomeOfficeSearchResponse.class);
        }
        return homeOfficeMultipleApplicantsResponse;
    }

    private void setUpMatchableAppellantStubbings() {
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(SECOND_SAMPLE_FIRST_NAME));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(SECOND_SAMPLE_LAST_NAME));
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of("1980-11-11"));
    }

    private void verifyMultipleAppellantsMatched(String jsonStr) {
        verify(asylumCase, times(1))
                .write(AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS, "MULTIPLE");
        verify(asylumCase, times(1))
                .write(HOME_OFFICE_SEARCH_STATUS_MESSAGE, HOME_OFFICE_MULTIPLE_APPELLANTS_ERROR_MESSAGE);
        verify(asylumCase, times(1)).write(HOME_OFFICE_SEARCH_RESPONSE, jsonStr);
    }

    private void setUpAppellantDetailStubbings() {
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(SECOND_SAMPLE_FIRST_NAME));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(SECOND_SAMPLE_LAST_NAME));
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of("1970-01-21"));
    }

    private void setUpCallbackStubbings() {
        when(featureToggler.getValue(HOME_OFFICE_UAN_FEATURE, false)).thenReturn(true);
        when(callback.getEvent()).thenReturn(SUBMIT_APPEAL);
        setUpCaseDetailsStubbings();
    }

    private void setUpCaseDetailsStubbings() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(caseId);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    private void assertResponseDataPopulated(PreSubmitCallbackResponse<AsylumCase> response) {
        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
    }

}
