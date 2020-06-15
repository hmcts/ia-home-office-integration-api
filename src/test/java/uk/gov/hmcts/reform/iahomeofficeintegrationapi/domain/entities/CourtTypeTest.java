package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import static junit.framework.TestCase.assertEquals;

import org.junit.jupiter.api.Test;

public class CourtTypeTest {

    @Test
    public void has_correct_values() {
        assertEquals("FIRST_TIER", CourtType.FIRST_TIER.toString());
        assertEquals("FTPA", CourtType.FTPA.toString());
        assertEquals("UTPA", CourtType.UTPA.toString());

    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(3, CourtType.values().length);
    }
}
