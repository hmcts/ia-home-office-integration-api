package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class DispatchPriorityTest {

    @Test
    public void has_correct_case_event_ids() {
        assertThat(DispatchPriority.EARLIEST.toString()).isEqualTo("earliest");
        assertThat(DispatchPriority.EARLY.toString()).isEqualTo("early");
        assertThat(DispatchPriority.LATE.toString()).isEqualTo("late");
        assertThat(DispatchPriority.LATEST.toString()).isEqualTo("latest");
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertThat(DispatchPriority.values().length).isEqualTo(4);
    }
}