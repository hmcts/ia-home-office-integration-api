package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class DispatchPriorityTest {

    @Test
    public void has_correct_case_event_ids() {
        assertThat(DispatchPriority.EARLIEST.toString()).hasToString("earliest");
        assertThat(DispatchPriority.EARLY.toString()).hasToString("early");
        assertThat(DispatchPriority.LATE.toString()).hasToString("late");
        assertThat(DispatchPriority.LATEST.toString()).hasToString("latest");
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertThat(DispatchPriority.values()).hasSize(4);
    }
}