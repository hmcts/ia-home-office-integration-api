package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.presubmit;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
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
import java.time.LocalDate;
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
public class AsylumCaseStatusSearchHandlerTest {

    private static final String HOME_OFFICE_CALL_ERROR_MESSAGE = "### There is a problem\n\n"
            + "The service has been unable t"
            + "o retrieve the Home Office information about this appeal.\n\n"
            + "[Request the Home Office information](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/"
            + "requestHomeOfficeData) to try again. This may take a few minutes.";

    private static final String HOME_OFFICE_INVALID_REFERENCE_ERROR_MESSAGE = "### There is a problem\n\n"
            + "The appellant entered the "
            + "Home Office reference number incorrectly. You can contact the appellant to check the reference number"
            + " if you need this information to validate the appeal";

    private static final String HOME_OFFICE_REFERENCE_NOT_FOUND_ERROR_MESSAGE = "### There is a problem\n\n"
            + "The appellant’s Home Office reference"
            + " number could not be found. You can contact the Home Office to check the reference"
            + " if you need this information to validate the appeal";

    private static final String HOME_OFFICE_APPELLANT_NOT_FOUND_ERROR_MESSAGE = "### There is a problem\n\n"
            + "The service has been unable to retrieve the Home Office information about this appeal "
            + "because the Home Office reference number does not have any matching appellant data in the system. "
            + "You can contact the Home Office if you need more information to validate the appeal.";

    private static final String HOME_OFFICE_MAIN_APPLICANT_NOT_FOUND_ERROR_MESSAGE = "**Note:** "
            + "The service was unable to retrieve any appellant details from the Home Office because the Home Office "
            + "data does not include a main applicant. You can contact the Home Office if you need this information "
            + "to validate the appeal.";

    private static HomeOfficeSearchResponse homeOfficeSearchResponse;
    private static HomeOfficeSearchResponse homeOfficeNullFieldResponse;
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

    @Value("classpath:home-office-sample-response.json")
    private Resource resource;
    @Value("classpath:home-office-null-field-response.json")
    private Resource resourceNullField;

    long caseId = 1234;

    private AsylumCaseStatusSearchHandler asylumCaseStatusSearchHandler;

    @BeforeEach
    void setUp() {
        asylumCaseStatusSearchHandler = new AsylumCaseStatusSearchHandler(homeOfficeSearchService, featureToggler);
    }

    @Test
    void check_handler_returns_case_data_with_home_office_fields() throws Exception {

        when(callback.getEvent()).thenReturn(SUBMIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(caseId);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of("Stephen"));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of("Fenn"));
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of("1970-01-21"));

        when(asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of(someHomeOfficeReference));
        when(asylumCase.read(HOME_OFFICE_CASE_STATUS_DATA, HomeOfficeCaseStatus.class))
                .thenReturn(Optional.of(caseStatus));
        when(homeOfficeSearchService.getCaseStatus(eq(caseId), anyString())).thenReturn(getSampleResponse());

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
        when(caseDetails.getId()).thenReturn(caseId);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of(someHomeOfficeReference));
        when(homeOfficeSearchService.getCaseStatus(eq(caseId), anyString()))
                .thenThrow(new HomeOfficeResponseException("some-error"));

        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of("Stephen"));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of("Fenn"));
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of("1970-01-21"));

        PreSubmitCallbackResponse<AsylumCase> response =
                asylumCaseStatusSearchHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        verify(asylumCase, times(1))
                .write(AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS, "FAIL");
        verify(asylumCase, times(1))
                .write(HOME_OFFICE_SEARCH_STATUS_MESSAGE, HOME_OFFICE_CALL_ERROR_MESSAGE);

    }

    @Test
    void check_get_formatted_decision_date_returns_date_when_date_is_geven() {

        LocalDate formattedDecisionDate = asylumCaseStatusSearchHandler.getFormattedDecisionDate("1998-01-30");

        assertThat(formattedDecisionDate).isNotNull();
        assertEquals(formattedDecisionDate,LocalDate.parse("1998-01-30"));

    }

    @Test
    void check_get_formatted_decision_date_returns_date_when_date_time_is_geven() {

        LocalDate formattedDecisionDate =
                asylumCaseStatusSearchHandler.getFormattedDecisionDate("2003-03-28T18:04:52Z");

        assertThat(formattedDecisionDate).isNotNull();
        assertEquals(formattedDecisionDate,LocalDate.parse("2003-03-28"));

    }

    @Test
    void check_handler_returns_case_data_with_error_status_for_null_fields() throws Exception {

        when(callback.getEvent()).thenReturn(REQUEST_HOME_OFFICE_DATA);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(caseId);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of(someHomeOfficeReference));
        when(homeOfficeSearchService.getCaseStatus(eq(caseId), anyString())).thenReturn(null);

        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of("Stephen"));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of("Fenn"));
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of("1970-01-21"));

        PreSubmitCallbackResponse<AsylumCase> response =
                asylumCaseStatusSearchHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        verify(asylumCase, times(1))
                .write(AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS, "FAIL");
        verify(asylumCase, times(1))
                .write(HOME_OFFICE_SEARCH_STATUS_MESSAGE, HOME_OFFICE_CALL_ERROR_MESSAGE);

    }

    @Test
    void handle_should_return_error_for_invalid_home_office_reference() {

        when(callback.getEvent()).thenReturn(PAY_AND_SUBMIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(caseId);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of("John"));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of("Smith"));
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of("1970-01-21"));
        when(asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of("1234-1234-1234-1234-1234-1234-1234-1234-1234-1234"));


        PreSubmitCallbackResponse<AsylumCase> response =
                asylumCaseStatusSearchHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);

        verify(asylumCase, times(1))
                .write(AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS, "FAIL");
        verify(asylumCase, times(1))
                .write(AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS_MESSAGE,
                        HOME_OFFICE_INVALID_REFERENCE_ERROR_MESSAGE);
    }

    @Test
    void check_handler_validates_person_null_value_from_home_office_data() throws Exception {

        when(callback.getEvent()).thenReturn(PAY_AND_SUBMIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(caseId);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of(someHomeOfficeReference));
        when(homeOfficeSearchService.getCaseStatus(eq(caseId), anyString())).thenReturn(getNullFieldResponse());

        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of("John"));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of("Smith"));
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of("1970-01-21"));

        PreSubmitCallbackResponse<AsylumCase> response =
                asylumCaseStatusSearchHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
    }

    @Test
    void check_handler_validates_error_detail_from_home_office_data_returns_fail() throws Exception {

        when(callback.getEvent()).thenReturn(MARK_APPEAL_PAID);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(caseId);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of(someHomeOfficeReference));
        when(homeOfficeSearchService.getCaseStatus(eq(caseId), anyString())).thenReturn(mockResponse);
        when(mockResponse.getErrorDetail()).thenReturn(new HomeOfficeError(
                "1020",
                "No document found",
                true
        ));

        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of("Stephen"));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of("Fenn"));
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of("1970-01-21"));

        PreSubmitCallbackResponse<AsylumCase> response =
                asylumCaseStatusSearchHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        verify(asylumCase, times(1))
                .write(AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS, "FAIL");
        verify(asylumCase, times(1))
                .write(HOME_OFFICE_SEARCH_STATUS_MESSAGE, HOME_OFFICE_REFERENCE_NOT_FOUND_ERROR_MESSAGE);

    }

    @Test
    void check_handler_validates_no_appellant_error_from_home_office_data_returns_fail() throws Exception {

        when(callback.getEvent()).thenReturn(SUBMIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(caseId);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of(someHomeOfficeReference));
        when(homeOfficeSearchService.getCaseStatus(eq(caseId), anyString())).thenReturn(mockResponse);
        when(mockResponse.getErrorDetail()).thenReturn(new HomeOfficeError(
                "1030",
                "No service details",
                true
        ));

        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of("Stephen"));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of("Fenn"));
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of("1970-01-21"));

        PreSubmitCallbackResponse<AsylumCase> response =
                asylumCaseStatusSearchHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        verify(asylumCase, times(1))
                .write(AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS, "FAIL");
        verify(asylumCase, times(1))
                .write(HOME_OFFICE_SEARCH_STATUS_MESSAGE, HOME_OFFICE_APPELLANT_NOT_FOUND_ERROR_MESSAGE);

    }

    @Test
    void check_handler_validates_ho_not_found_from_home_office_data_returns_fail() throws Exception {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(caseId);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of(someHomeOfficeReference));
        when(homeOfficeSearchService.getCaseStatus(eq(caseId), anyString())).thenReturn(mockResponse);
        when(mockResponse.getErrorDetail()).thenReturn(new HomeOfficeError(
                "1060",
                "UAN format is invalid",
                true
        ));

        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of("Stephen"));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of("Fenn"));
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of("1970-01-21"));

        PreSubmitCallbackResponse<AsylumCase> response =
                asylumCaseStatusSearchHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        verify(asylumCase, times(1))
                .write(AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS, "FAIL");
        verify(asylumCase, times(1))
                .write(HOME_OFFICE_SEARCH_STATUS_MESSAGE, HOME_OFFICE_INVALID_REFERENCE_ERROR_MESSAGE);

    }

    @Test
    void check_handler_validates_main_applicant_not_found_from_home_office_data_returns_fail() throws Exception {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(caseId);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of(someHomeOfficeReference));
        when(homeOfficeSearchService.getCaseStatus(eq(caseId), anyString())).thenReturn(mockResponse);

        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of("Stephen"));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of("Fenn"));
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of("1970-01-21"));

        PreSubmitCallbackResponse<AsylumCase> response =
                asylumCaseStatusSearchHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        verify(asylumCase, times(1))
                .write(AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS, "FAIL");
        verify(asylumCase, times(1))
                .write(HOME_OFFICE_SEARCH_STATUS_MESSAGE, HOME_OFFICE_MAIN_APPLICANT_NOT_FOUND_ERROR_MESSAGE);

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
    void it_can_handle_callback() {

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
        when(caseDetails.getId()).thenReturn(caseId);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        assertThatThrownBy(() -> asylumCaseStatusSearchHandler.handle(ABOUT_TO_SUBMIT, callback))
                .hasMessage("Home office reference for the appeal is not present, caseId: " + caseId)
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

        Person appellant = Person.PersonBuilder.person().withFamilyName("Fenn").withGivenName("Stephen").build();

        Optional<HomeOfficeCaseStatus> searchStatus = asylumCaseStatusSearchHandler.selectMainApplicant(
                caseId,
                getSampleResponse().getStatus(),
                appellant,
                "1970-01-21"
        );

        Person person = searchStatus.get().getPerson();

        assertNotNull(searchStatus);
        assertTrue(searchStatus.isPresent());
        assertThat(person.getFamilyName()).isEqualTo("Smith");
        assertThat(person.getGivenName()).isEqualTo("Capability");
    }

    @Test
    void should_match_applicant_details_by_given_and_family_names() throws Exception {

        Person appellant = Person.PersonBuilder.person().withFamilyName("Smith").withGivenName("Capability").build();

        Optional<HomeOfficeCaseStatus> searchStatus = asylumCaseStatusSearchHandler.selectMainApplicant(
                caseId,
                getSampleResponse().getStatus(),
                appellant,
                "1976-11-21"
        );

        Person person = searchStatus.get().getPerson();

        assertNotNull(searchStatus);
        assertTrue(searchStatus.isPresent());
        assertThat(person.getFamilyName()).isEqualTo("Smith");
        assertThat(person.getGivenName()).isEqualTo("Capability");

    }

    @Test
    void main_applicant_status_returned_empty_from_invalid_set_of_status() throws Exception {

        Person appellant = Person.PersonBuilder.person().withFamilyName("Fenn").withGivenName("Stephen").build();

        List<HomeOfficeCaseStatus> invalidList = new ArrayList<>();
        invalidList.add(getSampleResponse().getStatus().get(0));
        Optional<HomeOfficeCaseStatus> searchStatus =
                asylumCaseStatusSearchHandler.selectMainApplicant(
                        caseId,
                        invalidList,
                        appellant,
                        "1980-11-11"
                );

        assertNotNull(searchStatus);
        assertFalse(searchStatus.isPresent());
    }

    @Test
    void should_log_warning_for_no_metadata_of_applicant() throws Exception {

        Person appellant = Person.PersonBuilder.person().withFamilyName("Smith").withGivenName("Capability").build();

        Optional<HomeOfficeCaseStatus> searchStatus = asylumCaseStatusSearchHandler.selectMainApplicant(
                caseId,
                getSampleResponse().getStatus(),
                appellant,
                "1976-11-21"
        );

        Person person = searchStatus.get().getPerson();

        assertNotNull(searchStatus);
        assertTrue(searchStatus.isPresent());
        assertThat(person.getFamilyName()).isEqualTo("Smith");
        assertThat(person.getGivenName()).isEqualTo("Capability");
    }

    @Test
    void main_applicant_status_returned_empty_from_null_or_empty_list() {

        Person appellant = Person.PersonBuilder.person().withFamilyName("Fenn").withGivenName("Stephen").build();

        Optional<HomeOfficeCaseStatus> searchStatus =
                asylumCaseStatusSearchHandler.selectMainApplicant(
                        caseId,
                        null,
                        appellant,
                        "1980-11-11"
                );
        assertFalse(searchStatus.isPresent());
        searchStatus = asylumCaseStatusSearchHandler.selectMainApplicant(
                caseId,
                Collections.EMPTY_LIST,
                appellant,
                "1980-11-11"
        );
        assertFalse(searchStatus.isPresent());
    }

    @Test
    void metadata_returned_from_valid_set_of_values() throws Exception {

        Optional<HomeOfficeMetadata> metadata = asylumCaseStatusSearchHandler.selectMetadata(
                caseId,
                getSampleResponse().getStatus().get(1).getApplicationStatus().getHomeOfficeMetadata()
        );

        assertNotNull(metadata);
        assertTrue(metadata.isPresent());
    }

    @Test
    void metadata_returned_empty_from_invalid_set_of_values() throws Exception {
        Optional<HomeOfficeMetadata> metadata = asylumCaseStatusSearchHandler.selectMetadata(
                caseId,
                getSampleResponse().getStatus().get(0).getApplicationStatus().getHomeOfficeMetadata()
        );

        assertNotNull(metadata);
        assertFalse(metadata.isPresent());
    }

    @Test
    void metadata_returned_empty_from_null_or_empty_list() {
        Optional<HomeOfficeMetadata> metadata = asylumCaseStatusSearchHandler.selectMetadata(caseId,null);
        assertFalse(metadata.isPresent());
        metadata = asylumCaseStatusSearchHandler.selectMetadata(caseId, Collections.EMPTY_LIST);
        assertFalse(metadata.isPresent());
    }

    @Test
    void set_error_for_ho_reference_not_found_sets_values_in_asylum_case() {

        asylumCaseStatusSearchHandler.setErrorMessageForErrorCode(caseId, asylumCase, "1020", "Not found");
        verify(asylumCase, times(1))
                .write(AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS, "FAIL");
        verify(asylumCase, times(1))
                .write(HOME_OFFICE_SEARCH_STATUS_MESSAGE, HOME_OFFICE_REFERENCE_NOT_FOUND_ERROR_MESSAGE);

    }

    @Test
    void set_error_for_ho_appellant_not_found_sets_values_in_asylum_case() {

        asylumCaseStatusSearchHandler.setErrorMessageForErrorCode(caseId, asylumCase, "1010", "Not found");
        verify(asylumCase, times(1))
                .write(AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS, "FAIL");
        verify(asylumCase, times(1))
                .write(HOME_OFFICE_SEARCH_STATUS_MESSAGE, HOME_OFFICE_APPELLANT_NOT_FOUND_ERROR_MESSAGE);

    }

    @Test
    void set_error_for_invalid_format_sets_values_in_asylum_case() {

        asylumCaseStatusSearchHandler.setErrorMessageForErrorCode(caseId, asylumCase, "1060", "Format error");
        verify(asylumCase, times(1))
                .write(AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS, "FAIL");
        verify(asylumCase, times(1))
                .write(HOME_OFFICE_SEARCH_STATUS_MESSAGE, HOME_OFFICE_INVALID_REFERENCE_ERROR_MESSAGE);

    }

    @Test
    void set_error_for_general_error_sets_values_in_asylum_case() {

        asylumCaseStatusSearchHandler.setErrorMessageForErrorCode(caseId, asylumCase, null, null);
        verify(asylumCase, times(1))
                .write(AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS, "FAIL");
        verify(asylumCase, times(1))
                .write(HOME_OFFICE_SEARCH_STATUS_MESSAGE, HOME_OFFICE_CALL_ERROR_MESSAGE);

    }

    @Test
    void set_error_when_error_code_is_empty() {

        asylumCaseStatusSearchHandler.setErrorMessageForErrorCode(caseId, asylumCase, "", null);
        verify(asylumCase, times(1))
            .write(AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS, "FAIL");
        verify(asylumCase, times(1))
            .write(HOME_OFFICE_SEARCH_STATUS_MESSAGE, HOME_OFFICE_CALL_ERROR_MESSAGE);
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
