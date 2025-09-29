package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.controllers;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class AcceleratedAppealStatusControllerTest {


    private AcceleratedAppealStatusController acceleratedAppealStatusController = new AcceleratedAppealStatusController();

    @Test
    public void should_return_accelerated_appeal_status_response() {

        ResponseEntity<String> responseEntity = acceleratedAppealStatusController.welcome();

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertThat(
            responseEntity.getBody(),
            containsString("This endpoint is to be used for accelerated appeals only.")
        );
    }
}
