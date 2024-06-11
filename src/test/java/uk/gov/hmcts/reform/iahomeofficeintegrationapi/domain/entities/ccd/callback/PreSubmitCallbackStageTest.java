package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class PreSubmitCallbackStageTest {

    @Test
    public void has_correct_case_event_ids() {
        assertThat(PreSubmitCallbackStage.ABOUT_TO_START.toString()).hasToString("aboutToStart");
        assertThat(PreSubmitCallbackStage.ABOUT_TO_SUBMIT.toString()).hasToString("aboutToSubmit");
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertThat(PreSubmitCallbackStage.values()).hasSize(2);
    }
}