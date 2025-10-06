package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.controllers;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.SubmitEventDetails;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeFasterCaseStatusDto;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.service.CcdDataService;

import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security.S2STokenValidator.SERVICE_AUTHORIZATION_HEADER;

@Tag(name = "Update Home Office faster case status controller")
@OpenAPIDefinition(tags = {@Tag(name = "UpdateHomeOfficeFasterCaseStatusController", description = "Update Home Office faster case status")})
@RestController
@Slf4j
public class UpdateHomeOfficeFasterCaseStatusController {

    private final CcdDataService ccdDataService;

    public UpdateHomeOfficeFasterCaseStatusController(CcdDataService ccdDataService) {
        this.ccdDataService = ccdDataService;
    }

    @Operation(
        summary = "Update Home Office faster case status",
        responses =
            {
                @ApiResponse(
                    responseCode = "200",
                    description = "Updated Home Office faster case status successfully",
                    content = @Content(schema = @Schema(implementation = String.class))),
                @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request",
                    content = @Content(schema = @Schema(implementation = String.class))),
                @ApiResponse(
                    responseCode = "403",
                    description = "Calling service is not authorised to use the endpoint",
                    content = @Content(schema = @Schema(implementation = String.class))),
                @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = String.class)))

            }
    )

    @PutMapping(path = "/home-office-faster-case-updates",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SubmitEventDetails> updateHomeOfficeFasterCaseStatus(
        @RequestHeader(value = SERVICE_AUTHORIZATION_HEADER) String s2sAuthToken,
        @RequestBody HomeOfficeFasterCaseStatusDto hoFasterCaseDto
    ) {
        SubmitEventDetails response = ccdDataService.updateHomeOfficeFasterCaseStatus(hoFasterCaseDto);
        return ResponseEntity.status(response.getCallbackResponseStatusCode()).body(response);
    }

}
