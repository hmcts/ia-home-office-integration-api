package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.CaseNotFoundException;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeStatutoryTimeframeDto;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.SubmitEventDetails;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.service.CcdDataService;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.State;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SetHomeOfficeStatutoryTimeframeStatusControllerTest {

    @Mock
    private CcdDataService ccdDataService;

    @Mock
    private HomeOfficeStatutoryTimeframeDto hoStatutoryTimeframeDto;

    @Mock
    private SubmitEventDetails submitEventDetails;

    @InjectMocks
    private SetHomeOfficeStatutoryTimeframeStatusController controller;

    @BeforeEach
    void setUp() {
        controller = new SetHomeOfficeStatutoryTimeframeStatusController(ccdDataService);
    }

    @Test
    void should_return_s2s_token_successfully() {
        // Given
        String expectedToken = "test-s2s-token-value";
        when(ccdDataService.generateS2SToken()).thenReturn(expectedToken);

        // When
        ResponseEntity<String> response = controller.getS2SToken();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedToken);
        verify(ccdDataService).generateS2SToken();
    }

    @Test
    void should_call_ccd_data_service_to_generate_s2s_token() {
        // Given
        String expectedToken = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
        when(ccdDataService.generateS2SToken()).thenReturn(expectedToken);

        // When
        controller.getS2SToken();

        // Then
        verify(ccdDataService).generateS2SToken();
    }

    @Test
    void should_update_statutory_timeframe_status_successfully() {
        // Given
        String s2sToken = "Bearer test-token";
        when(submitEventDetails.getCallbackResponseStatusCode()).thenReturn(200);
        when(ccdDataService.setHomeOfficeStatutoryTimeframeStatus(hoStatutoryTimeframeDto))
            .thenReturn(submitEventDetails);

        // When
        ResponseEntity<SubmitEventDetails> response = 
            controller.updateHomeOfficeStatutoryTimeframeStatus(s2sToken, hoStatutoryTimeframeDto);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(submitEventDetails);
        verify(ccdDataService).setHomeOfficeStatutoryTimeframeStatus(hoStatutoryTimeframeDto);
    }

    @Test
    void should_return_success_response_when_update_home_office_statutory_timeframe_status() {
        // Given
        String s2sAuthToken = "test-token";
        HomeOfficeStatutoryTimeframeDto dto = new HomeOfficeStatutoryTimeframeDto();
        SubmitEventDetails expectedResponse = new SubmitEventDetails(
            1L,
            "IA",
            State.APPEAL_SUBMITTED,
            new HashMap<>(),
            HttpStatus.OK.value(),
            "OK"
        );

        when(ccdDataService.setHomeOfficeStatutoryTimeframeStatus(dto))
            .thenReturn(expectedResponse);

        // When
        ResponseEntity<SubmitEventDetails> response = controller
            .updateHomeOfficeStatutoryTimeframeStatus(s2sAuthToken, dto);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    void getS2SToken_shouldReturnS2SToken() {
        String expectedToken = "test-s2s-token";
        when(ccdDataService.generateS2SToken()).thenReturn(expectedToken);

        ResponseEntity<String> response = controller.getS2SToken();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedToken);
        verify(ccdDataService).generateS2SToken();
    }

    @Test
    void getServiceUserToken_shouldReturnServiceUserToken() {
        String expectedToken = "test-service-user-token";
        when(ccdDataService.getServiceUserToken()).thenReturn(expectedToken);

        ResponseEntity<String> response = controller.getServiceUserToken();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedToken);
        verify(ccdDataService).getServiceUserToken();
    }

    @Test
    void handleCaseNotFoundException_shouldReturn404NotFound() {
        // Given
        CaseNotFoundException exception = new CaseNotFoundException("Case not found for caseId: 12345");

        // When
        ResponseEntity<String> response = controller.handleCaseNotFoundException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).contains("Case not found for caseId: 12345");
    }

    @Test
    void handleValidationException_shouldReturn400BadRequest() throws NoSuchMethodException {
        // Given
        HomeOfficeStatutoryTimeframeDto dto = new HomeOfficeStatutoryTimeframeDto();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(dto, "hoStatutoryTimeframeDto");
        bindingResult.addError(new FieldError("hoStatutoryTimeframeDto", "stf24weeks", "must not be null"));

        MethodParameter methodParameter = new MethodParameter(
            SetHomeOfficeStatutoryTimeframeStatusController.class.getMethod(
                "updateHomeOfficeStatutoryTimeframeStatus", String.class, HomeOfficeStatutoryTimeframeDto.class), 1);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

        // When
        ResponseEntity<String> response = controller.handleValidationException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("stf24weeks");
        assertThat(response.getBody()).contains("must not be null");
    }
}
