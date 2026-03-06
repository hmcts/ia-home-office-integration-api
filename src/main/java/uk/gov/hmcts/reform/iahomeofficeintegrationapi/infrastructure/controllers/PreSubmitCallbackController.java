package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.controllers;

import static java.util.Objects.requireNonNull;
import static org.springframework.http.ResponseEntity.ok;

import org.springframework.http.ResponseEntity;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.PreSubmitCallbackDispatcher;

@Slf4j
public class PreSubmitCallbackController<T extends CaseData> {

    protected final PreSubmitCallbackDispatcher<T> callbackDispatcher;

    public PreSubmitCallbackController(
        PreSubmitCallbackDispatcher<T> callbackDispatcher
    ) {
        requireNonNull(callbackDispatcher, "callbackDispatcher must not be null");

        this.callbackDispatcher = callbackDispatcher;
    }

    public ResponseEntity<PreSubmitCallbackResponse<T>> ccdAboutToStart(
        Callback<T> callback
    ) {
        return performStageRequest(PreSubmitCallbackStage.ABOUT_TO_START, callback);
    }

    public ResponseEntity<PreSubmitCallbackResponse<T>> ccdAboutToSubmit(
        Callback<T> callback
    ) {
        return performStageRequest(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
    }

    public ResponseEntity<PreSubmitCallbackResponse<T>> ccdMidEvent(
        Callback<T> callback, String pageId
    ) {
        if (pageId != null) {
            callback.setPageId(pageId);
        }
        return performStageRequest(PreSubmitCallbackStage.MID_EVENT, callback);
    }

    ResponseEntity<PreSubmitCallbackResponse<T>> performStageRequest(
        PreSubmitCallbackStage callbackStage,
        Callback<T> callback
    ) {
        // Log pageId if mid-event
        if (callbackStage.equals(PreSubmitCallbackStage.MID_EVENT)) {
            log.info(
                "Asylum Case CCD `{}` event `{}` received for Case ID `{}` from page ID `{}`",
                callbackStage,
                callback.getEvent(),
                callback.getCaseDetails().getId(),
                callback.getPageId()
            );
        } else {
            log.info(
                "Asylum Case CCD `{}` event `{}` received for Case ID `{}`",
                callbackStage,
                callback.getEvent(),
                callback.getCaseDetails().getId()
            );
        }

        PreSubmitCallbackResponse<T> callbackResponse =
            callbackDispatcher.handle(callbackStage, callback);

        if (!callbackResponse.getErrors().isEmpty()) {
            log.warn(
                "Asylum Case CCD `{}` event `{}` handled for Case ID `{}` with errors `{}`",
                callbackStage,
                callback.getEvent(),
                callback.getCaseDetails().getId(),
                callbackResponse.getErrors()
            );
        } else {

            log.info(
                "Asylum Case CCD `{}` event `{}` handled for Case ID `{}`",
                callbackStage,
                callback.getEvent(),
                callback.getCaseDetails().getId()
            );
        }

        return ok(callbackResponse);
    }
}
