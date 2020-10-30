package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import static junit.framework.TestCase.assertEquals;

import org.junit.jupiter.api.Test;

class CourtTypeTest {

    @Test
    void has_correct_values() {
        assertEquals("FIRST_TIER", CourtType.FIRST_TIER.toString());
        assertEquals("UPPER_TRIBUNAL", CourtType.UPPER_TRIBUNAL.toString());
        assertEquals("SUPREME_COURT", CourtType.SUPREME_COURT.toString());
        assertEquals("COURT_OF_APPEAL", CourtType.COURT_OF_APPEAL.toString());

    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(4, CourtType.values().length);
    }
}
