package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

import static org.assertj.core.api.Assertions.assertThat;

public class PreSubmitCallbackHandlerTest implements PreSubmitCallbackHandler {

    @Test
    public void default_dispatch_priority_is_late() {
        assertThat(this.getDispatchPriority()).isEqualTo(DispatchPriority.LATE);
    }

    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback callback) {
        return false;
    }

    public PreSubmitCallbackResponse handle(PreSubmitCallbackStage callbackStage, Callback callback) {
        return null;
    }
}
