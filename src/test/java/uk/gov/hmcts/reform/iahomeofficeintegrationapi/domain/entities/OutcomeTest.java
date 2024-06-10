package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class OutcomeTest {

    @Test
    void has_correct_values() {
        assertThat(Outcome.ALLOWED.toString()).isEqualTo("ALLOWED");
        assertThat(Outcome.DISMISSED.toString()).isEqualTo("DISMISSED");
        assertThat(Outcome.GRANTED.toString()).isEqualTo("GRANTED");
        assertThat(Outcome.REFUSED.toString()).isEqualTo("REFUSED");
        assertThat(Outcome.REHEARD.toString()).isEqualTo("REHEARD");
        assertThat(Outcome.REMADE.toString()).isEqualTo("REMADE");
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertThat(Outcome.values().length).isEqualTo(6);
    }
}
