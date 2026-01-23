package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.controllers;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.SubmitEventDetails;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeStatutoryTimeframeDto;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.service.CcdDataService;
import org.springframework.web.bind.annotation.GetMapping;

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
        @RequestBody HomeOfficeStatutoryTimeframeDto hoStatutoryTimeframeDto
    ) {
        log.info("HTTP POST /home-office-statutory-timeframe-status endpoint called with payload: {}", hoStatutoryTimeframeDto);
        SubmitEventDetails response = ccdDataService.setHomeOfficeStatutoryTimeframeStatus(hoStatutoryTimeframeDto);
        return ResponseEntity.status(response.getCallbackResponseStatusCode()).body(response);
    }

    @Operation(
        summary = "Get S2S token",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Returns S2S token",
                content = @Content(schema = @Schema(implementation = String.class))
                )
        }
    )
    
    //Do not merge this endpoint. It used only for exploring purposes to get S2S token.
    //Remove it from anonymous path too before merging.
    @GetMapping(path = "/s2stoken", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getS2SToken() {
        log.info("HTTP GET /s2stoken endpoint called");
        String s2sToken = ccdDataService.generateS2SToken();
        return ResponseEntity.ok(s2sToken);
    }

    @Operation(
        summary = "Get service user token",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Returns service user token",
                content = @Content(schema = @Schema(implementation = String.class))
                )
        }
    )
    //Do not merge this endpoint. It used only for exploring purposes to get service user token.
    //Remove it from anonymous path too before merging.
    @GetMapping(path = "/serviceusertoken", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getServiceUserToken() {
        log.info("HTTP GET /serviceusertoken endpoint called");
        String serviceUserToken = ccdDataService.getServiceUserToken();
        return ResponseEntity.ok(serviceUserToken);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalStateException(IllegalStateException ex) {
        log.error("Conflict error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

}
