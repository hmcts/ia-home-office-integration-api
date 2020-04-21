package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.controllers;

import static org.springframework.http.ResponseEntity.ok;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Default endpoints per application.
 */
@RestController
public class WelcomeController {

    private final String s2sMicroserviceKey;

    public WelcomeController(
        @Value("${idam.s2s-auth.microservice}") String s2sMicroserviceKey
    ) {
        this.s2sMicroserviceKey = s2sMicroserviceKey;
    }

    /**
     * Root GET endpoint.
     *
     * <p>Azure application service has a hidden feature of making requests to root endpoint when
     * "Always On" is turned on.
     * This is the endpoint to deal with that and therefore silence the unnecessary 404s as a response code.
     *
     * @return Welcome message from the service.
     */
    @GetMapping("/")
    public ResponseEntity<String> welcome() {
        return ok("Welcome to Home Office Integration API s2sMicroservice=" + s2sMicroserviceKey);
    }
}
