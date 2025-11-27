package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.YesOrNo;

@SuppressWarnings("unchecked")
class StatutoryTimeFrame24WeeksFieldValueTest {

    private StatutoryTimeframe24Weeks statutoryTimeFrame24WeeksFieldValue;

    @Test
    void has_correct_values() {

        statutoryTimeFrame24WeeksFieldValue = new StatutoryTimeframe24Weeks(
            YesOrNo.YES,
            "Test reason",
            "Test user",
            "2024-01-01T10:00:00Z"
        );
        
        assertEquals(YesOrNo.YES, statutoryTimeFrame24WeeksFieldValue.getStatus());
        assertEquals("Test reason", statutoryTimeFrame24WeeksFieldValue.getReason());
        assertEquals("Test user", statutoryTimeFrame24WeeksFieldValue.getUser());
        assertEquals("2024-01-01T10:00:00Z", statutoryTimeFrame24WeeksFieldValue.getDateAdded());
    }

    @Test
    void should_create_object_with_all_fields() {
        YesOrNo status = YesOrNo.YES;
        String reason = "Test reason";
        String user = "Test user";
        String dateAdded = "2023-01-01T10:00:00Z";

        StatutoryTimeframe24Weeks fieldValue = new StatutoryTimeframe24Weeks(
            status, reason, user, dateAdded
        );

        assertEquals(status, fieldValue.getStatus());
        assertEquals(reason, fieldValue.getReason());
        assertEquals(user, fieldValue.getUser());
        assertEquals(dateAdded, fieldValue.getDateAdded());
    }

    @Test
    void equals_should_return_true_for_same_values() {
        StatutoryTimeframe24Weeks fieldValue1 = new StatutoryTimeframe24Weeks(
            YesOrNo.YES,
            "Test reason",
            "Test user",
            "2024-01-01T10:00:00Z"
        );

        StatutoryTimeframe24Weeks fieldValue2 = new StatutoryTimeframe24Weeks(
            YesOrNo.YES,
            "Test reason",
            "Test user",
            "2024-01-01T10:00:00Z"
        );

        assertTrue(fieldValue1.equals(fieldValue2));
    }

    @Test
    void toString_should_return_string_representation() {
        statutoryTimeFrame24WeeksFieldValue = new StatutoryTimeframe24Weeks(
            YesOrNo.YES,
            "Test reason",
            "Test user",
            "2024-01-01T10:00:00Z"
        );

        String result = statutoryTimeFrame24WeeksFieldValue.toString();

        assertNotNull(result);
    }

    @Test
    void should_be_equal_when_all_fields_are_same() {
        StatutoryTimeframe24Weeks value1 = new StatutoryTimeframe24Weeks(
            YesOrNo.YES, "reason1", "user1", "2020-06-15T17:35:38Z"
        );
        StatutoryTimeframe24Weeks value2 = new StatutoryTimeframe24Weeks(
            YesOrNo.YES, "reason1", "user1", "2020-06-15T17:35:38Z"
        );

        assertEquals(value1, value2);
        assertEquals(value1.hashCode(), value2.hashCode());
    }

    @Test
    void should_not_be_equal_when_fields_differ() {
        StatutoryTimeframe24Weeks value1 = new StatutoryTimeframe24Weeks(
            YesOrNo.YES, "reason1", "user1", "2020-06-15T17:35:38Z"
        );
        StatutoryTimeframe24Weeks value2 = new StatutoryTimeframe24Weeks(
            YesOrNo.NO, "reason1", "user1", "2020-06-15T17:35:38Z"
        );

        assertNotEquals(value1, value2);
    }

    @Test
    void should_not_be_equal_to_null_or_different_class() {
        StatutoryTimeframe24Weeks value = new StatutoryTimeframe24Weeks(
            YesOrNo.YES, "reason1", "user1", "2020-06-15T17:35:38Z"
        );

        assertNotEquals(value, null);
        assertNotEquals(value, "string");
    }
}
