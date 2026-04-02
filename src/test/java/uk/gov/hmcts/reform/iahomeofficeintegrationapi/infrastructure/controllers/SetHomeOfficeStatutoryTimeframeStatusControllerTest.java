package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.CaseNotFoundException;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeStatutoryTimeframeDto;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.SubmitEventDetails;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.service.CcdDataService;

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
    void should_update_statutory_timeframe_status_successfully() throws Exception {
        // Given
        String s2sToken = "Bearer test-token";
        when(submitEventDetails.getCallbackResponseStatusCode()).thenReturn(200);
        when(ccdDataService.setHomeOfficeStatutoryTimeframeStatus(hoStatutoryTimeframeDto))
            .thenReturn(submitEventDetails);

        // When
        ResponseEntity<HomeOfficeStatutoryTimeframeDto> response = 
            controller.updateHomeOfficeStatutoryTimeframeStatus(s2sToken, hoStatutoryTimeframeDto);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(hoStatutoryTimeframeDto);
        verify(ccdDataService).setHomeOfficeStatutoryTimeframeStatus(hoStatutoryTimeframeDto);
    }

    @Test
    void should_return_success_response_when_update_home_office_statutory_timeframe_status() throws Exception {
        // Given
        String s2sAuthToken = "test-token";

        HomeOfficeStatutoryTimeframeDto dto =
            HomeOfficeStatutoryTimeframeDto.builder()
                .hmctsReferenceNumber("PA/12345/2026")
                .uan("1234-5678-9012-3456")
                .familyName("Smith")
                .givenNames("John")
                .dateOfBirth(LocalDate.of(1990, 1, 15))
                .stf24weekCohorts(List.of(
                    HomeOfficeStatutoryTimeframeDto.Stf24WeekCohort.builder()
                        .name("cohortA")
                        .included("true")
                        .build()
                ))
                .timeStamp(OffsetDateTime.now())
                .build();

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
        ResponseEntity<HomeOfficeStatutoryTimeframeDto> response = controller
            .updateHomeOfficeStatutoryTimeframeStatus(s2sAuthToken, dto);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(dto, response.getBody());
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
