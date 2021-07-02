package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.presubmit;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_APPELLANTS_LIST;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER_BEFORE_EDIT;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_SEARCH_NO_MATCH;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_SEARCH_RESPONSE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS_MESSAGE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event.REQUEST_HOME_OFFICE_DATA;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.CodeWithDescription;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeCaseStatus;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeSearchResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.Person;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.DynamicList;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Value;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.FeatureToggler;

@SpringJUnitConfig
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class RequestHomeOfficeDataHandlerTest {

    private static final String PROBLEM_MESSAGE = "### There is a problem\n\n";

    private static final String HOME_OFFICE_CALL_ERROR_MESSAGE = PROBLEM_MESSAGE
            + "The service has been unable t"
            + "o retrieve the Home Office information about this appeal.\n\n"
            + "[Request the Home Office information](/case/IA/Asylum/${[CASE_REFERENCE]}/"
            + "trigger/requestHomeOfficeData) to try again. This may take a few minutes.";

    private static HomeOfficeSearchResponse homeOfficeSearchResponse;
    @Mock
    private FeatureToggler featureToggler;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Spy
    private HomeOfficeDataErrorsHelper homeOfficeDataErrorsHelper;

    @org.springframework.beans.factory.annotation.Value("classpath:home-office-sample-response.json")
    private Resource resource;

    long caseId = 1234;

    private RequestHomeOfficeDataHandler requestHomeOfficeDataHandler;

    @BeforeEach
    void setUp() {

        requestHomeOfficeDataHandler =
                new RequestHomeOfficeDataHandler(homeOfficeDataErrorsHelper, featureToggler);
    }

    @Test
    void handler_should_error_if_appellant_not_selected() {

        when(featureToggler.getValue("home-office-uan-feature", false)).thenReturn(true);
        when(callback.getEvent()).thenReturn(REQUEST_HOME_OFFICE_DATA);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(caseId);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        assertThatThrownBy(() -> requestHomeOfficeDataHandler.handle(ABOUT_TO_SUBMIT,  callback))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("Appellant not selected form the list.");
    }

    @Test
    void reject_reasons_returned_as_one_string_formatted() throws Exception {

        String rejectReason = requestHomeOfficeDataHandler.getRejectionReasonString(
                getSampleResponse().getStatus().get(1).getApplicationStatus().getRejectionReasons());

        assertNotNull(rejectReason);
        assertEquals("Application not completed properly" + "<br />" + "Application not entered properly",
                rejectReason);

    }

    @Test
    void reject_reasons_returned_as_empty_string_for_null_or_empty_value() {

        String rejectReason = requestHomeOfficeDataHandler.getRejectionReasonString(null);
        assertEquals("", rejectReason);
        rejectReason = requestHomeOfficeDataHandler.getRejectionReasonString(new ArrayList<>());
        assertEquals("", rejectReason);
    }

    @Test
    void handle_should_return_no_match_if_no_appellant_matched() throws Exception {

        when(featureToggler.getValue("home-office-uan-feature", false)).thenReturn(true);
        when(callback.getEvent()).thenReturn(REQUEST_HOME_OFFICE_DATA);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(caseId);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(HOME_OFFICE_APPELLANTS_LIST, DynamicList.class)).thenReturn(Optional.of(getNoMatch()));

        PreSubmitCallbackResponse<AsylumCase> response =
                requestHomeOfficeDataHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertTrue(response.getErrors().isEmpty());

        verify(asylumCase, times(1)).write(HOME_OFFICE_SEARCH_STATUS, "SUCCESS");
        verify(asylumCase, times(1)).write(HOME_OFFICE_SEARCH_NO_MATCH, "NO_MATCH");
    }

    @Test
    void handle_should_return_matched_appellant_details() throws Exception {

        String hoSearchResponseJsonStr = new ObjectMapper().writeValueAsString(getSampleResponse());

        when(featureToggler.getValue("home-office-uan-feature", false)).thenReturn(true);
        when(callback.getEvent()).thenReturn(REQUEST_HOME_OFFICE_DATA);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(caseId);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(HOME_OFFICE_APPELLANTS_LIST, DynamicList.class))
                .thenReturn(Optional.of(selectAppellant()));
        when(asylumCase.read(HOME_OFFICE_SEARCH_RESPONSE, String.class))
                .thenReturn(Optional.of(hoSearchResponseJsonStr));

        PreSubmitCallbackResponse<AsylumCase> response =
                requestHomeOfficeDataHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertTrue(response.getErrors().isEmpty());

        verify(asylumCase, times(1)).write(HOME_OFFICE_SEARCH_STATUS, "SUCCESS");
        verify(asylumCase, times(1)).clear(HOME_OFFICE_SEARCH_RESPONSE);
        verify(asylumCase, times(1)).clear(HOME_OFFICE_REFERENCE_NUMBER_BEFORE_EDIT);
    }

    @Test
    void handler_should_throw_error_for_no_search_response_data() {

        when(featureToggler.getValue("home-office-uan-feature", false)).thenReturn(true);
        when(callback.getEvent()).thenReturn(REQUEST_HOME_OFFICE_DATA);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(caseId);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(HOME_OFFICE_APPELLANTS_LIST, DynamicList.class))
                .thenReturn(Optional.of(selectAppellant()));

        PreSubmitCallbackResponse<AsylumCase> response =
                requestHomeOfficeDataHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        verify(asylumCase, times(1))
                .write(AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS, "FAIL");
        verify(asylumCase, times(1))
                .write(HOME_OFFICE_SEARCH_STATUS_MESSAGE, HOME_OFFICE_CALL_ERROR_MESSAGE);
    }

    @Test
    void handler_should_throw_error_for_empty_ho_search_response_data() throws Exception {

        when(featureToggler.getValue("home-office-uan-feature", false)).thenReturn(true);
        when(callback.getEvent()).thenReturn(REQUEST_HOME_OFFICE_DATA);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(caseId);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(HOME_OFFICE_APPELLANTS_LIST, DynamicList.class))
                .thenReturn(Optional.of(selectAppellant()));
        when(asylumCase.read(HOME_OFFICE_SEARCH_RESPONSE, String.class)).thenReturn(Optional.of(""));

        PreSubmitCallbackResponse<AsylumCase> response =
                requestHomeOfficeDataHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        verify(asylumCase, times(1))
                .write(AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS, "FAIL");
        verify(asylumCase, times(1))
                .write(HOME_OFFICE_SEARCH_STATUS_MESSAGE, HOME_OFFICE_CALL_ERROR_MESSAGE);
    }

    @Test
    void handler_should_throw_if_event_not_applicable() {

        when(callback.getEvent()).thenReturn(Event.UNKNOWN);

        assertThatThrownBy(() -> requestHomeOfficeDataHandler.handle(ABOUT_TO_SUBMIT, callback))
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

                boolean canHandle = requestHomeOfficeDataHandler.canHandle(callbackStage, callback);

                if (callbackStage == ABOUT_TO_SUBMIT
                        && (callback.getEvent() == REQUEST_HOME_OFFICE_DATA)
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

        assertThatThrownBy(() -> requestHomeOfficeDataHandler.canHandle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> requestHomeOfficeDataHandler.canHandle(ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> requestHomeOfficeDataHandler.handle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> requestHomeOfficeDataHandler.handle(ABOUT_TO_SUBMIT, null))
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

    private DynamicList selectAppellant() {

        List<Value> values = new ArrayList<>();
        values.add(new Value("Capability Smith", "Capability Smith-210170"));
        return new DynamicList(values.get(0), values);
    }

    private DynamicList getNoMatch() {

        List<Value> values = new ArrayList<>();
        values.add(new Value("NoMatch", "No Match"));

        return new DynamicList(values.get(0), values);
    }

    private HomeOfficeCaseStatus getNoMatchResponse() {

        String noMatch = "No match";
        Person noMatchingPerson = Person.PersonBuilder.person()
                .withGivenName(noMatch)
                .withFamilyName(noMatch)
                .withNationality(new CodeWithDescription(noMatch, noMatch))
                .withGender(new CodeWithDescription(noMatch, noMatch))
                .build();

        return new HomeOfficeCaseStatus(noMatchingPerson, null, null,
                null, null, null,
                null, null,
                null, null);

    }
}
