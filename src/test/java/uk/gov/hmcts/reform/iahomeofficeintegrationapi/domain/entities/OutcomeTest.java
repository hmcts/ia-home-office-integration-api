package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import static junit.framework.TestCase.assertEquals;

import org.junit.jupiter.api.Test;

public class OutcomeTest {

    @Test
    public void has_correct_values() {
        assertEquals("ALLOWED", Outcome.ALLOWED.toString());
        assertEquals("DISMISSED", Outcome.DISMISSED.toString());
        assertEquals("GRANTED", Outcome.GRANTED.toString());
        assertEquals("REFUSED", Outcome.REFUSED.toString());
        assertEquals("REHEARD", Outcome.REHEARD.toString());
        assertEquals("REMADE", Outcome.REMADE.toString());

    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(6, Outcome.values().length);
    }
}
