package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.controllers;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.CaseNotFoundException;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.SubmitEventDetails;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeStatutoryTimeframeDto;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.HomeOfficeResponseException;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.service.CcdDataService;

@Tag(name = "Set Home Office statutory timeframe status controller")
@OpenAPIDefinition(tags = {@Tag(name = "SetHomeOfficeStatutoryTimeframeStatusController", description = "Set Home Office statutory timeframe status")})
@RestController
@Slf4j
public class SetHomeOfficeStatutoryTimeframeStatusController {

    public static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";

    private final CcdDataService ccdDataService;

    public SetHomeOfficeStatutoryTimeframeStatusController(CcdDataService ccdDataService) {
        this.ccdDataService = ccdDataService;
    }

    @Operation(
        summary = "Set Home Office statutory timeframe status",
        responses =
            {
                @ApiResponse(
                    responseCode = "201",
                    description = "Set Home Office statutory timeframe status successfully",
                    content = @Content(schema = @Schema(implementation = String.class))),
                @ApiResponse(
                    responseCode = "400",
                    description = "Home Office statutory timeframe status request body failed validation",
                    content = @Content(schema = @Schema(implementation = String.class))),
                @ApiResponse(
                    responseCode = "401",
                    description = "This endpoint requires authentication",
                    content = @Content(schema = @Schema(implementation = String.class))),
                @ApiResponse(
                    responseCode = "403",
                    description = "Calling service is not authorised to use this endpoint",
                    content = @Content(schema = @Schema(implementation = String.class))),
                @ApiResponse(
                    responseCode = "404",
                    description = "Case not found",
                    content = @Content(schema = @Schema(implementation = String.class))),
                @ApiResponse(
                    responseCode = "409",
                    description = "Statutory timeframe status has already been set for this case",
                    content = @Content(schema = @Schema(implementation = String.class))),
                @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = String.class)))
            }
    )

    @PostMapping(path = "/home-office-statutory-timeframe-status",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SubmitEventDetails> updateHomeOfficeStatutoryTimeframeStatus(
        @RequestHeader(value = SERVICE_AUTHORIZATION_HEADER) String s2sAuthToken,
        @Valid @RequestBody HomeOfficeStatutoryTimeframeDto hoStatutoryTimeframeDto
    ) {
        log.info("HTTP POST /home-office-statutory-timeframe-status endpoint called with payload: {}", hoStatutoryTimeframeDto);
        SubmitEventDetails response = ccdDataService.setHomeOfficeStatutoryTimeframeStatus(hoStatutoryTimeframeDto);
        return ResponseEntity.status(response.getCallbackResponseStatusCode()).body(response);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalStateException(IllegalStateException ex) {
        log.error("Conflict error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(CaseNotFoundException.class)
    public ResponseEntity<String> handleCaseNotFoundException(CaseNotFoundException ex) {
        log.error("Case not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\":\"" + ex.getMessage() + "\"}");
    }

    @ExceptionHandler(HomeOfficeResponseException.class)
    public ResponseEntity<String> handleHomeOfficeResponseException(HomeOfficeResponseException ex) {
        String message = ex.getMessage();
        if (message != null && message.contains("Case ID is not valid")) {
            log.error("Case not found: {}", message);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\":\"Case not found\"}");
        }
        log.error("Home Office response error: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + message + "\"}");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .reduce((a, b) -> a + ", " + b)
            .orElse("Validation failed");
        log.error("Validation error: {}", errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + errorMessage + "\"}");
    }

}
