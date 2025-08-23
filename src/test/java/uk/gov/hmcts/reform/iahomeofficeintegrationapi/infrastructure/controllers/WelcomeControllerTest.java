package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.controllers;

import static org.hamcrest.Matchers.containsString;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class WelcomeControllerTest {


    private WelcomeController welcomeController = new WelcomeController();

    @Test
    void should_return_welcome_response() {

        ResponseEntity<String> responseEntity = welcomeController.welcome();

        Assertions.assertNotNull(responseEntity);
        Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        MatcherAssert.assertThat(responseEntity.getBody(),
                containsString("Welcome to Home Office Integration API"));
    }
}
