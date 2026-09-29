package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.controllers;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeStatutoryTimeframeDto;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.SubmitEventDetails;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.service.CcdDataService;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    void should_update_statutory_timeframe_status_successfully() throws Exception {
        // Given
        String s2sToken = "Bearer test-token";
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
                .stf24weekCohortDtos(List.of(
                    HomeOfficeStatutoryTimeframeDto.Stf24WeekCohortDto.builder()
                        .name("cohortA")
                        .included(true)
                        .build()
                ))
                .timeStamp(OffsetDateTime.now())
                .build();

        SubmitEventDetails expectedResponse = new SubmitEventDetails(
            1L,
            "IA",
            State.APPEAL_SUBMITTED,
            Map.of("something", "something"),
            HttpStatus.OK.value(),
            "OK",
            null
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
    void should_log_and_throw_if_event_submission_throws() {
        Logger responseLogger = (Logger) LoggerFactory.getLogger(SetHomeOfficeStatutoryTimeframeStatusController.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        responseLogger.addAppender(listAppender);

        when(ccdDataService.setHomeOfficeStatutoryTimeframeStatus(hoStatutoryTimeframeDto))
            .thenThrow(new NullPointerException("some exception error message"));

        RuntimeException runtimeException = assertThrows(NullPointerException.class, () ->
            controller.updateHomeOfficeStatutoryTimeframeStatus("Bearer test-token", hoStatutoryTimeframeDto));

        List<ILoggingEvent> logEvents = listAppender.list;
        assertEquals(1, logEvents.size());
        ILoggingEvent loggingEvent = logEvents.getFirst();
        assertEquals("HTTP POST to /home-office-statutory-timeframe-status endpoint was unsuccessful.",
            loggingEvent.getFormattedMessage());
        assertEquals(Level.ERROR, loggingEvent.getLevel());

        assertEquals("some exception error message", runtimeException.getMessage());
    }
}
