package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Default endpoints per application.
 */
@Slf4j
@RestController
public class AcceleratedAppealStatusController {

    /**
     * GET endpoint for accelerated appeals.
     *
     * @return Status message.
     */
    @GetMapping("/acceleratedAppeal")
    public ResponseEntity<String> welcome() {

        final String message = "This endpoint is to be used for accelerated appeals only.";

        return ResponseEntity
            .ok()
            .cacheControl(CacheControl.noCache())
            .contentType(MediaType.APPLICATION_JSON)
            .body("{\"message\": \"" + message + "\"}");
    }
}
