package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class OutcomeTest {

    @Test
    void has_correct_values() {
        assertThat(Outcome.ALLOWED.toString()).hasToString("ALLOWED");
        assertThat(Outcome.DISMISSED.toString()).hasToString("DISMISSED");
        assertThat(Outcome.GRANTED.toString()).hasToString("GRANTED");
        assertThat(Outcome.REFUSED.toString()).hasToString("REFUSED");
        assertThat(Outcome.REHEARD.toString()).hasToString("REHEARD");
        assertThat(Outcome.REMADE.toString()).hasToString("REMADE");
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertThat(Outcome.values()).hasSize(6);
    }
}
