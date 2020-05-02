package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

public class PreSubmitCallbackHandlerTest implements PreSubmitCallbackHandler {

    @Test
    public void default_dispatch_priority_is_late() {
        Assertions.assertEquals(DispatchPriority.LATE, this.getDispatchPriority());
    }

    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback callback) {
        return false;
    }

    public PreSubmitCallbackResponse handle(PreSubmitCallbackStage callbackStage, Callback callback) {
        return null;
    }
}
