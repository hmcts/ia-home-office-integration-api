package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.controllers;

import static java.util.Objects.requireNonNull;
import static org.springframework.http.ResponseEntity.ok;


import javax.validation.constraints.NotNull;
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
        requireNonNull(callbackDispatcher, "callbackDispatcher must not be null");

        this.callbackDispatcher = callbackDispatcher;
    }



    @PostMapping(path = "/ccdAboutToStart")
    public ResponseEntity<PreSubmitCallbackResponse<AsylumCase>> ccdAboutToStart(@NotNull @RequestBody Callback<AsylumCase> callback
    ) {
        return performStageRequest(PreSubmitCallbackStage.ABOUT_TO_START, callback);
    }


    @PostMapping(path = "/ccdAboutToSubmit")
    public ResponseEntity<PreSubmitCallbackResponse<AsylumCase>> ccdAboutToSubmit(@NotNull @RequestBody Callback<AsylumCase> callback) {

        return performStageRequest(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

    }

    private ResponseEntity<PreSubmitCallbackResponse<AsylumCase>> performStageRequest(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {

        log.info(
            "Asylum Case CCD `{}` event `{}` received for Case ID `{}`",
            callbackStage,
            callback.getEvent(),
            callback.getCaseDetails().getId()
        );

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            callbackDispatcher.handle(callbackStage, callback);

        log.info(
            "Asylum Case CCD `{}` event `{}` handled for Case ID `{}`",
            callbackStage,
            callback.getEvent(),
            callback.getCaseDetails().getId()
        );

        return ok(callbackResponse);
    }
}
