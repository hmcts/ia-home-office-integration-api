package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import static junit.framework.TestCase.assertEquals;

import org.junit.jupiter.api.Test;

public class MessageTypeTest {

    @Test
    public void has_correct_values() {
        assertEquals("REQUEST_CHALLENGE_END", MessageType.REQUEST_CHALLENGE_END.toString());
        assertEquals("REQUEST_EVIDENCE_BUNDLE", MessageType.REQUEST_EVIDENCE_BUNDLE.toString());
        assertEquals("REQUEST_REVIEW", MessageType.REQUEST_REVIEW.toString());
        assertEquals("HEARING", MessageType.HEARING.toString());
        assertEquals("HEARING_BUNDLE_READY", MessageType.HEARING_BUNDLE_READY.toString());
        assertEquals("COURT_OUTCOME", MessageType.COURT_OUTCOME.toString());
        assertEquals("DEFAULT", MessageType.DEFAULT.toString());

    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(7, MessageType.values().length);
    }
}
