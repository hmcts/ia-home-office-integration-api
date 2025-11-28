package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.YesOrNo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class StatutoryTimeframe24WeeksHistoryTest {

    @Test
    void shouldCreateStatutoryTimeframe24WeeksHistoryWithValidData() {
        // Given
        YesOrNo status = YesOrNo.YES;
        String reason = "Test reason";
        String user = "Test user";
        String dateTimeAdded = "2024-01-01T12:00:00";

        // When
        StatutoryTimeframe24WeeksHistory history = new StatutoryTimeframe24WeeksHistory(
            status, reason, user, dateTimeAdded
        );

        // Then
        assertNotNull(history);
        assertEquals(YesOrNo.YES, history.getStatus());
        assertEquals("Test reason", history.getReason());
        assertEquals("Test user", history.getUser());
        assertEquals("2024-01-01T12:00:00", history.getDateTimeAdded());
    }

    @Test
    void shouldThrowNullPointerExceptionWhenStatusIsNull() {
        // When & Then
        assertThrows(NullPointerException.class, () -> 
            new StatutoryTimeframe24WeeksHistory(null, "reason", "user", "2024-01-01T12:00:00")
        );
    }

    @Test
    void shouldThrowNullPointerExceptionWhenReasonIsNull() {
        // When & Then
        assertThrows(NullPointerException.class, () -> 
            new StatutoryTimeframe24WeeksHistory(YesOrNo.YES, null, "user", "2024-01-01T12:00:00")
        );
    }

    @Test
    void shouldThrowNullPointerExceptionWhenUserIsNull() {
        // When & Then
        assertThrows(NullPointerException.class, () -> 
            new StatutoryTimeframe24WeeksHistory(YesOrNo.YES, "reason", null, "2024-01-01T12:00:00")
        );
    }

    @Test
    void shouldThrowNullPointerExceptionWhenDateTimeAddedIsNull() {
        // When & Then
        assertThrows(NullPointerException.class, () -> 
            new StatutoryTimeframe24WeeksHistory(YesOrNo.YES, "reason", "user", null)
        );
    }

    @Test
    void shouldBeEqualWhenSameValues() {
        // Given
        StatutoryTimeframe24WeeksHistory history1 = new StatutoryTimeframe24WeeksHistory(
            YesOrNo.YES, "Test reason", "Test user", "2024-01-01T12:00:00"
        );
        StatutoryTimeframe24WeeksHistory history2 = new StatutoryTimeframe24WeeksHistory(
            YesOrNo.YES, "Test reason", "Test user", "2024-01-01T12:00:00"
        );

        // Then
        assertEquals(history1, history2);
        assertEquals(history1.hashCode(), history2.hashCode());
        assertTrue(history1.equals(history2));
        assertTrue(history2.equals(history1));
    }

    @Test
    void shouldNotBeEqualWhenDifferentStatus() {
        // Given
        StatutoryTimeframe24WeeksHistory history1 = new StatutoryTimeframe24WeeksHistory(
            YesOrNo.YES, "Test reason", "Test user", "2024-01-01T12:00:00"
        );
        StatutoryTimeframe24WeeksHistory history2 = new StatutoryTimeframe24WeeksHistory(
            YesOrNo.NO, "Test reason", "Test user", "2024-01-01T12:00:00"
        );

        // Then
        assertNotEquals(history1, history2);
        assertNotEquals(history1.hashCode(), history2.hashCode());
        assertFalse(history1.equals(history2));
    }

    @Test
    void shouldNotBeEqualWhenDifferentReason() {
        // Given
        StatutoryTimeframe24WeeksHistory history1 = new StatutoryTimeframe24WeeksHistory(
            YesOrNo.YES, "Test reason 1", "Test user", "2024-01-01T12:00:00"
        );
        StatutoryTimeframe24WeeksHistory history2 = new StatutoryTimeframe24WeeksHistory(
            YesOrNo.YES, "Test reason 2", "Test user", "2024-01-01T12:00:00"
        );

        // Then
        assertNotEquals(history1, history2);
        assertFalse(history1.equals(history2));
    }

    @Test
    void shouldNotBeEqualWhenDifferentUser() {
        // Given
        StatutoryTimeframe24WeeksHistory history1 = new StatutoryTimeframe24WeeksHistory(
            YesOrNo.YES, "Test reason", "Test user 1", "2024-01-01T12:00:00"
        );
        StatutoryTimeframe24WeeksHistory history2 = new StatutoryTimeframe24WeeksHistory(
            YesOrNo.YES, "Test reason", "Test user 2", "2024-01-01T12:00:00"
        );

        // Then
        assertNotEquals(history1, history2);
        assertFalse(history1.equals(history2));
    }

    @Test
    void shouldNotBeEqualWhenDifferentDateTime() {
        // Given
        StatutoryTimeframe24WeeksHistory history1 = new StatutoryTimeframe24WeeksHistory(
            YesOrNo.YES, "Test reason", "Test user", "2024-01-01T12:00:00"
        );
        StatutoryTimeframe24WeeksHistory history2 = new StatutoryTimeframe24WeeksHistory(
            YesOrNo.YES, "Test reason", "Test user", "2024-01-02T12:00:00"
        );

        // Then
        assertNotEquals(history1, history2);
        assertFalse(history1.equals(history2));
    }

    @Test
    void shouldBeEqualToItself() {
        // Given
        StatutoryTimeframe24WeeksHistory history = new StatutoryTimeframe24WeeksHistory(
            YesOrNo.YES, "Test reason", "Test user", "2024-01-01T12:00:00"
        );

        // Then
        assertEquals(history, history);
        assertEquals(history.hashCode(), history.hashCode());
        assertTrue(history.equals(history));
    }

    @Test
    void shouldNotBeEqualToNull() {
        // Given
        StatutoryTimeframe24WeeksHistory history = new StatutoryTimeframe24WeeksHistory(
            YesOrNo.YES, "Test reason", "Test user", "2024-01-01T12:00:00"
        );

        // Then
        assertNotEquals(null, history);
        assertFalse(history.equals(null));
    }

    @Test
    void shouldNotBeEqualToDifferentType() {
        // Given
        StatutoryTimeframe24WeeksHistory history = new StatutoryTimeframe24WeeksHistory(
            YesOrNo.YES, "Test reason", "Test user", "2024-01-01T12:00:00"
        );

        // Then
        assertNotEquals("different type", history);
        assertFalse(history.equals("different type"));
        assertFalse(history.equals(new Object()));
    }

    @Test
    void shouldHaveConsistentToString() {
        // Given
        StatutoryTimeframe24WeeksHistory history1 = new StatutoryTimeframe24WeeksHistory(
            YesOrNo.YES, "Test reason", "Test user", "2024-01-01T12:00:00"
        );
        StatutoryTimeframe24WeeksHistory history2 = new StatutoryTimeframe24WeeksHistory(
            YesOrNo.YES, "Test reason", "Test user", "2024-01-01T12:00:00"
        );

        // Then
        assertNotNull(history1.toString());
        assertEquals(history1.toString(), history2.toString());
    }

    @Test
    void testEqualsAndHashCode() {
        // Given
        StatutoryTimeframe24WeeksHistory history1 = new StatutoryTimeframe24WeeksHistory(
            YesOrNo.YES,
            "Reason 1",
            "User 1",
            "2023-01-01T10:00:00"
        );

        StatutoryTimeframe24WeeksHistory history2 = new StatutoryTimeframe24WeeksHistory(
            YesOrNo.YES,
            "Reason 1",
            "User 1",
            "2023-01-01T10:00:00"
        );

        StatutoryTimeframe24WeeksHistory history3 = new StatutoryTimeframe24WeeksHistory(
            YesOrNo.NO,
            "Reason 2",
            "User 2",
            "2023-01-02T10:00:00"
        );

        // Then
        assertEquals(history1, history2);
        assertEquals(history1.hashCode(), history2.hashCode());
        
        assertNotEquals(history1, history3);
        assertNotEquals(history1.hashCode(), history3.hashCode());
        
        // Symmetric
        assertTrue(history1.equals(history2));
        assertTrue(history2.equals(history1));
        
        // Transitive
        StatutoryTimeframe24WeeksHistory history4 = new StatutoryTimeframe24WeeksHistory(
            YesOrNo.YES,
            "Reason 1",
            "User 1",
            "2023-01-01T10:00:00"
        );
        assertTrue(history1.equals(history2));
        assertTrue(history2.equals(history4));
        assertTrue(history1.equals(history4));
    }

    @Test
    void shouldHaveDifferentHashCodeWhenDifferentFields() {
        // Given
        StatutoryTimeframe24WeeksHistory history1 = new StatutoryTimeframe24WeeksHistory(
            YesOrNo.YES, "Reason A", "User A", "2024-01-01T10:00:00"
        );
        StatutoryTimeframe24WeeksHistory history2 = new StatutoryTimeframe24WeeksHistory(
            YesOrNo.NO, "Reason B", "User B", "2024-01-02T10:00:00"
        );

        // Then - different objects should likely have different hash codes
        assertNotEquals(history1.hashCode(), history2.hashCode());
    }

    @Test
    void shouldHandleEmptyStrings() {
        // Given
        StatutoryTimeframe24WeeksHistory history1 = new StatutoryTimeframe24WeeksHistory(
            YesOrNo.YES, "", "", ""
        );
        StatutoryTimeframe24WeeksHistory history2 = new StatutoryTimeframe24WeeksHistory(
            YesOrNo.YES, "", "", ""
        );

        // Then
        assertEquals(history1, history2);
        assertEquals(history1.hashCode(), history2.hashCode());
    }
}
