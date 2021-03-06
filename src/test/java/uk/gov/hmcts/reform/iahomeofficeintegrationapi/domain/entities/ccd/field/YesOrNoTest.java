package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field;

import static junit.framework.TestCase.assertEquals;

import org.junit.jupiter.api.Test;

class YesOrNoTest {

    @Test
    void has_correct_values() {
        assertEquals("No", YesOrNo.NO.toString());
        assertEquals("Yes", YesOrNo.YES.toString());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(2, YesOrNo.values().length);
    }
}
