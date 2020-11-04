package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import static junit.framework.TestCase.assertEquals;

import org.junit.jupiter.api.Test;

class MessageTypeTest {

    @Test
    void has_correct_values() {
        assertEquals("REQUEST_CHALLENGE_END", MessageType.REQUEST_CHALLENGE_END.name());
        assertEquals("REQUEST_EVIDENCE_BUNDLE", MessageType.REQUEST_EVIDENCE_BUNDLE.name());
        assertEquals("REQUEST_REVIEW", MessageType.REQUEST_REVIEW.name());
        assertEquals("HEARING", MessageType.HEARING.name());
        assertEquals("HEARING_BUNDLE_READY", MessageType.HEARING_BUNDLE_READY.name());
        assertEquals("COURT_OUTCOME", MessageType.COURT_OUTCOME.name());
        assertEquals("PERMISSION_TO_APPEAL", MessageType.PERMISSION_TO_APPEAL.name());
        assertEquals("DEFAULT", MessageType.DEFAULT.name());

    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(8, MessageType.values().length);
    }
}
