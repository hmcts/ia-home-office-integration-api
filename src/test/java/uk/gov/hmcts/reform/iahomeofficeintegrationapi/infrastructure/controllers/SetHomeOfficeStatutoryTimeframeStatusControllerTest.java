package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeStatutoryTimeframeDto;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.SubmitEventDetails;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.service.CcdDataService;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.State;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SetHomeOfficeStatutoryTimeframeStatusControllerTest {

    @Mock
    private CcdDataService ccdDataService;

    @InjectMocks
    private SetHomeOfficeStatutoryTimeframeStatusController controller;

    @Test
    void should_return_success_response_when_update_home_office_statutory_timeframe_status() {
        // Given
        // String s2sAuthToken = "test-token";
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
            .updateHomeOfficeStatutoryTimeframeStatus(dto);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
    }
}
