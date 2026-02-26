package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.presubmit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_APPELLANTS;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_APPELLANT_API_HTTP_STATUS;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_APPELLANT_CLAIM_DATE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_APPELLANT_DECISION_DATE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_APPELLANT_DECISION_LETTER_DATE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.HomeOfficeAppellant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeAppellantDto;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeApplicationDto;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.HomeOfficeApplicationService;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.HomeOfficeMissingApplicationException;

class GetAppellantDataHandlerTest {

    @Mock
    private HomeOfficeApplicationService homeOfficeApplicationService;

    @Mock
    private FeatureToggler featureToggler;

    @Mock
    private Callback<AsylumCase> callback;

    @Mock
    private CaseDetails<AsylumCase> caseDetails;

    @Mock
    private AsylumCase asylumCase;

    @Captor
    private ArgumentCaptor<AsylumCase> asylumCaseCaptor;

    @InjectMocks
    private GetAppellantDataHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(featureToggler.getValue("home-office-uan-feature", false)).thenReturn(true);
    }

    @Test
    void canHandle_returnsTrue_whenStageAndEventAndPageIdCorrect() {
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getPageId()).thenReturn("homeOfficeReferenceNumber");

        boolean result = handler.canHandle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertTrue(result);
    }

    @Test
    void canHandle_returnsFalse_WrongPageId() {
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getPageId()).thenReturn("thisPageDoesNotExist");

        boolean result = handler.canHandle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertFalse(result);
    }

    @Test
    void canHandle_returnsFalse_WrongEvent() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(callback.getPageId()).thenReturn("thisPageDoesNotExist");

        boolean result = handler.canHandle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertFalse(result);
    }

    @Test
    void canHandle_returnsFalse_WrongStage() {
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getPageId()).thenReturn("homeOfficeReferenceNumber");

        boolean result = handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertFalse(result);
    }

    @Test
    void handle_writesData_whenServiceReturnsApplication() throws Exception {
        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);
        when(callback.getPageId()).thenReturn("appellantBasicDetails");
        when(caseDetails.getId()).thenReturn(12345L);
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("UAN123"));

        // Use LocalDate for test clarity
        final LocalDate claimDate = LocalDate.of(2026, 1, 1);
        final LocalDate decisionDate = LocalDate.of(2026, 1, 2);
        final LocalDate decisionLetterDate = LocalDate.of(2026, 1, 3);

        HomeOfficeAppellantDto appellantDto = new HomeOfficeAppellantDto();
        appellantDto.setFamilyName("Smith");
        appellantDto.setGivenNames("John");
        appellantDto.setDateOfBirth(LocalDate.of(1980, 5, 5));
        appellantDto.setNationality("British");
        appellantDto.setRoa(true);
        appellantDto.setAsylumSupport(false);
        appellantDto.setHoFeeWaiver(true);
        appellantDto.setInterpreterNeeded(false);
        appellantDto.setLanguage("English");

        HomeOfficeApplicationDto applicationDto = new HomeOfficeApplicationDto();
        applicationDto.setHoClaimDate(claimDate);
        applicationDto.setHoDecisionDate(decisionDate);
        applicationDto.setHoDecisionLetterDate(decisionLetterDate);
        applicationDto.setAppellants(List.of(appellantDto));

        when(homeOfficeApplicationService.getApplication("UAN123")).thenReturn(applicationDto);

        PreSubmitCallbackResponse<AsylumCase> response = handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());

        verify(asylumCase).write(HOME_OFFICE_APPELLANT_CLAIM_DATE, claimDate);
        verify(asylumCase).write(HOME_OFFICE_APPELLANT_DECISION_DATE, decisionDate);
        verify(asylumCase).write(HOME_OFFICE_APPELLANT_DECISION_LETTER_DATE, decisionLetterDate);
        verify(asylumCase).write(eq(HOME_OFFICE_APPELLANTS), any(HomeOfficeAppellant.class));
        verify(asylumCase).write(HOME_OFFICE_APPELLANT_API_HTTP_STATUS, "200");
    }

    @Test
    void handle_writesHttpStatus_whenServiceThrowsException() throws Exception {
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getPageId()).thenReturn("oocHomeOfficeReferenceNumber");
        when(caseDetails.getId()).thenReturn(12345L);
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("UAN123"));

        when(homeOfficeApplicationService.getApplication("UAN123"))
            .thenThrow(new HomeOfficeMissingApplicationException(404, "Not found"));

        PreSubmitCallbackResponse<AsylumCase> response = handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());

        verify(asylumCase).write(HOME_OFFICE_APPELLANT_API_HTTP_STATUS, 404);
    }

    @Test
    void handle_throwsException_whenCannotHandle() {
        when(featureToggler.getValue("home-office-uan-feature", false)).thenReturn(false);

        assertThrows(IllegalStateException.class,
            () -> handler.handle(PreSubmitCallbackStage.MID_EVENT, callback));
    }
}
