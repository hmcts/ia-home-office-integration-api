package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.controllers;

import static java.util.Objects.requireNonNull;
import static org.springframework.http.ResponseEntity.ok;

import javax.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.PreSubmitCallbackDispatcher;

@Tag(name = "Asylum Service")
@RequestMapping(
        path = "/asylum",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
)

@Slf4j
@RestController
public class PreSubmitCallbackController {

    private final PreSubmitCallbackDispatcher<AsylumCase> callbackDispatcher;

    public PreSubmitCallbackController(
            PreSubmitCallbackDispatcher<AsylumCase> callbackDispatcher
    ) {
        System.out.println("Inside Home office Api PreSubmitCallbackController");
        requireNonNull(callbackDispatcher, "callbackDispatcher must not be null");

        this.callbackDispatcher = callbackDispatcher;
    }

    @Operation(
            summary = "Handles 'AboutToStartEvent' callbacks from CCD",
            security =
                    {
                            @SecurityRequirement(name = "Authorization"),
                            @SecurityRequirement(name = "ServiceAuthorization")
                    },
            responses =
                    {
                            @ApiResponse(
                                    responseCode = "200",
                                    description = "Transformed Asylum case data, with any identified error or warning messages",
                                    content = @Content(schema = @Schema(implementation = PreSubmitCallbackResponse.class))),

                            @ApiResponse(
                                    responseCode = "400",
                                    description = "Bad Request",
                                    content = @Content(schema = @Schema(implementation = PreSubmitCallbackResponse.class))),

                            @ApiResponse(
                                    responseCode = "403",
                                    description = "Forbidden",
                                    content = @Content(schema = @Schema(implementation = PreSubmitCallbackResponse.class))),

                            @ApiResponse(
                                    responseCode = "415",
                                    description = "Unsupported Media Type",
                                    content = @Content(schema = @Schema(implementation = PreSubmitCallbackResponse.class))),

                            @ApiResponse(
                                    responseCode = "500",
                                    description = "Internal Server Error",
                                    content = @Content(schema = @Schema(implementation = PreSubmitCallbackResponse.class)))

                    }
    )


    @PostMapping(path = "/ccdAboutToStart")
    public ResponseEntity<PreSubmitCallbackResponse<AsylumCase>> ccdAboutToStart(
            @Parameter(name = "Asylum case data", required = true) @NotNull @RequestBody Callback<AsylumCase> callback
    ) {
        System.out.println("Home office PreSubmitCallbackResponse/ccdAboutToStart");
        return performStageRequest(PreSubmitCallbackStage.ABOUT_TO_START, callback);
    }

    @Operation(
            summary = "Transformed Asylum case data with Home Office information, with any identified error or "
                    + "warning messages",
            security =
                    {
                            @SecurityRequirement(name = "Authorization"),
                            @SecurityRequirement(name = "ServiceAuthorization")
                    },
            responses =
                    {
                            @ApiResponse(
                                    responseCode = "200",
                                    description = "Optional confirmation text for CCD UI",
                                    content = @Content(schema = @Schema(implementation = PreSubmitCallbackResponse.class))),

                            @ApiResponse(
                                    responseCode = "400",
                                    description = "Bad Request",
                                    content = @Content(schema = @Schema(implementation = PreSubmitCallbackResponse.class))),

                            @ApiResponse(
                                    responseCode = "403",
                                    description = "Forbidden",
                                    content = @Content(schema = @Schema(implementation = PreSubmitCallbackResponse.class))),

                            @ApiResponse(
                                    responseCode = "415",
                                    description = "Unsupported Media Type",
                                    content = @Content(schema = @Schema(implementation = PreSubmitCallbackResponse.class))),


                            @ApiResponse(
                                    responseCode = "500",
                                    description = "Internal Server Error",
                                    content = @Content(schema = @Schema(implementation = PreSubmitCallbackResponse.class)))

                    }
    )

    @PostMapping(path = "/ccdAboutToSubmit")
    public ResponseEntity<PreSubmitCallbackResponse<AsylumCase>> ccdAboutToSubmit(
            @Parameter(name = "Asylum case data", required = true) @NotNull @RequestBody Callback<AsylumCase> callback) {
        System.out.println("Home office PreSubmitCallbackResponse/ccdAboutToSubmit");
        return performStageRequest(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

    }

    private ResponseEntity<PreSubmitCallbackResponse<AsylumCase>> performStageRequest(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {

        log.info(
                "HomeOffice API Asylum Case CCD `{}` event `{}` received for Case ID `{}`",
                callbackStage,
                callback.getEvent(),
                callback.getCaseDetails().getId()
        );

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                callbackDispatcher.handle(callbackStage, callback);

        log.info(
                "performStageRequest2 Asylum Case CCD `{}` event `{}` handled for Case ID `{}`",
                callbackStage,
                callback.getEvent(),
                callback.getCaseDetails().getId()
        );

        return ok(callbackResponse);
    }
}
