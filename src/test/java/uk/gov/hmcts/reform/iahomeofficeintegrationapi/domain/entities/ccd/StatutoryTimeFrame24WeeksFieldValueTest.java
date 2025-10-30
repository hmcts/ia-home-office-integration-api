package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.YesOrNo;

@SuppressWarnings("unchecked")
class StatutoryTimeFrame24WeeksFieldValueTest {

    private StatutoryTimeFrame24WeeksFieldValue statutoryTimeFrame24WeeksFieldValue;

    @Test
    void has_correct_values() {

        statutoryTimeFrame24WeeksFieldValue = new StatutoryTimeFrame24WeeksFieldValue(
            YesOrNo.YES,
            "Test reason",
            "Test user",
            "2024-01-01T10:00:00Z"
        );
        
        assertEquals(YesOrNo.YES, statutoryTimeFrame24WeeksFieldValue.getStatus());
        assertEquals("Test reason", statutoryTimeFrame24WeeksFieldValue.getReason());
        assertEquals("Test user", statutoryTimeFrame24WeeksFieldValue.getUser());
        assertEquals("2024-01-01T10:00:00Z", statutoryTimeFrame24WeeksFieldValue.getDateTimeAdded());
    }

    @Test
    void should_create_object_with_all_fields() {
        YesOrNo status = YesOrNo.YES;
        String reason = "Test reason";
        String user = "Test user";
        String dateAdded = "2023-01-01T10:00:00Z";

        StatutoryTimeFrame24WeeksFieldValue fieldValue = new StatutoryTimeFrame24WeeksFieldValue(
            status, reason, user, dateAdded
        );

        assertEquals(status, fieldValue.getStatus());
        assertEquals(reason, fieldValue.getReason());
        assertEquals(user, fieldValue.getUser());
        assertEquals(dateAdded, fieldValue.getDateTimeAdded());
    }

    @Test
    void equals_should_return_true_for_same_values() {
        StatutoryTimeFrame24WeeksFieldValue fieldValue1 = new StatutoryTimeFrame24WeeksFieldValue(
            YesOrNo.YES,
            "Test reason",
            "Test user",
            "2024-01-01T10:00:00Z"
        );

        StatutoryTimeFrame24WeeksFieldValue fieldValue2 = new StatutoryTimeFrame24WeeksFieldValue(
            YesOrNo.YES,
            "Test reason",
            "Test user",
            "2024-01-01T10:00:00Z"
        );

        assertTrue(fieldValue1.equals(fieldValue2));
    }

    @Test
    void toString_should_return_string_representation() {
        statutoryTimeFrame24WeeksFieldValue = new StatutoryTimeFrame24WeeksFieldValue(
            YesOrNo.YES,
            "Test reason",
            "Test user",
            "2024-01-01T10:00:00Z"
        );

        String result = statutoryTimeFrame24WeeksFieldValue.toString();

        assertNotNull(result);
    }

    @Test
    void equals_should_return_true_for_same_object() {
        StatutoryTimeFrame24WeeksFieldValue fieldValue = new StatutoryTimeFrame24WeeksFieldValue(
            YesOrNo.YES,
            "Test reason",
            "Test user",
            "2024-01-01T10:00:00Z"
        );

        assertTrue(fieldValue.equals(fieldValue));
    }

    @Test
    void equals_should_return_false_for_null() {
        StatutoryTimeFrame24WeeksFieldValue fieldValue = new StatutoryTimeFrame24WeeksFieldValue(
            YesOrNo.YES,
            "Test reason",
            "Test user",
            "2024-01-01T10:00:00Z"
        );

        assertFalse(fieldValue.equals(null));
    }

    @Test
    void equals_should_return_false_for_different_class() {
        StatutoryTimeFrame24WeeksFieldValue fieldValue = new StatutoryTimeFrame24WeeksFieldValue(
            YesOrNo.YES,
            "Test reason",
            "Test user",
            "2024-01-01T10:00:00Z"
        );

        assertFalse(fieldValue.equals("different class"));
    }

    @Test
    void equals_should_return_false_when_status_differs() {
        StatutoryTimeFrame24WeeksFieldValue fieldValue1 = new StatutoryTimeFrame24WeeksFieldValue(
            YesOrNo.YES,
            "Test reason",
            "Test user",
            "2024-01-01T10:00:00Z"
        );

        StatutoryTimeFrame24WeeksFieldValue fieldValue2 = new StatutoryTimeFrame24WeeksFieldValue(
            YesOrNo.NO,
            "Test reason",
            "Test user",
            "2024-01-01T10:00:00Z"
        );

        assertNotEquals(fieldValue1, fieldValue2);
    }

    @Test
    void equals_should_return_false_when_reason_differs() {
        StatutoryTimeFrame24WeeksFieldValue fieldValue1 = new StatutoryTimeFrame24WeeksFieldValue(
            YesOrNo.YES,
            "Test reason",
            "Test user",
            "2024-01-01T10:00:00Z"
        );

        StatutoryTimeFrame24WeeksFieldValue fieldValue2 = new StatutoryTimeFrame24WeeksFieldValue(
            YesOrNo.YES,
            "Different reason",
            "Test user",
            "2024-01-01T10:00:00Z"
        );

        assertNotEquals(fieldValue1, fieldValue2);
    }

    @Test
    void equals_should_return_false_when_user_differs() {
        StatutoryTimeFrame24WeeksFieldValue fieldValue1 = new StatutoryTimeFrame24WeeksFieldValue(
            YesOrNo.YES,
            "Test reason",
            "Test user",
            "2024-01-01T10:00:00Z"
        );

        StatutoryTimeFrame24WeeksFieldValue fieldValue2 = new StatutoryTimeFrame24WeeksFieldValue(
            YesOrNo.YES,
            "Test reason",
            "Different user",
            "2024-01-01T10:00:00Z"
        );

        assertNotEquals(fieldValue1, fieldValue2);
    }

    @Test
    void equals_should_return_false_when_dateTimeAdded_differs() {
        StatutoryTimeFrame24WeeksFieldValue fieldValue1 = new StatutoryTimeFrame24WeeksFieldValue(
            YesOrNo.YES,
            "Test reason",
            "Test user",
            "2024-01-01T10:00:00Z"
        );

        StatutoryTimeFrame24WeeksFieldValue fieldValue2 = new StatutoryTimeFrame24WeeksFieldValue(
            YesOrNo.YES,
            "Test reason",
            "Test user",
            "2024-02-01T10:00:00Z"
        );

        assertNotEquals(fieldValue1, fieldValue2);
    }

    @Test
    void equals_should_be_symmetric() {
        StatutoryTimeFrame24WeeksFieldValue fieldValue1 = new StatutoryTimeFrame24WeeksFieldValue(
            YesOrNo.YES,
            "Test reason",
            "Test user",
            "2024-01-01T10:00:00Z"
        );

        StatutoryTimeFrame24WeeksFieldValue fieldValue2 = new StatutoryTimeFrame24WeeksFieldValue(
            YesOrNo.YES,
            "Test reason",
            "Test user",
            "2024-01-01T10:00:00Z"
        );

        assertTrue(fieldValue1.equals(fieldValue2));
        assertTrue(fieldValue2.equals(fieldValue1));
    }

    @Test
    void equals_should_be_transitive() {
        StatutoryTimeFrame24WeeksFieldValue fieldValue1 = new StatutoryTimeFrame24WeeksFieldValue(
            YesOrNo.YES,
            "Test reason",
            "Test user",
            "2024-01-01T10:00:00Z"
        );

        StatutoryTimeFrame24WeeksFieldValue fieldValue2 = new StatutoryTimeFrame24WeeksFieldValue(
            YesOrNo.YES,
            "Test reason",
            "Test user",
            "2024-01-01T10:00:00Z"
        );

        StatutoryTimeFrame24WeeksFieldValue fieldValue3 = new StatutoryTimeFrame24WeeksFieldValue(
            YesOrNo.YES,
            "Test reason",
            "Test user",
            "2024-01-01T10:00:00Z"
        );

        assertTrue(fieldValue1.equals(fieldValue2));
        assertTrue(fieldValue2.equals(fieldValue3));
        assertTrue(fieldValue1.equals(fieldValue3));
    }

    @Test
    void hashCode_should_be_consistent() {
        StatutoryTimeFrame24WeeksFieldValue fieldValue = new StatutoryTimeFrame24WeeksFieldValue(
            YesOrNo.YES,
            "Test reason",
            "Test user",
            "2024-01-01T10:00:00Z"
        );

        int hashCode1 = fieldValue.hashCode();
        int hashCode2 = fieldValue.hashCode();

        assertEquals(hashCode1, hashCode2);
    }

    @Test
    void hashCode_should_be_equal_for_equal_objects() {
        StatutoryTimeFrame24WeeksFieldValue fieldValue1 = new StatutoryTimeFrame24WeeksFieldValue(
            YesOrNo.YES,
            "Test reason",
            "Test user",
            "2024-01-01T10:00:00Z"
        );

        StatutoryTimeFrame24WeeksFieldValue fieldValue2 = new StatutoryTimeFrame24WeeksFieldValue(
            YesOrNo.YES,
            "Test reason",
            "Test user",
            "2024-01-01T10:00:00Z"
        );

        assertEquals(fieldValue1.hashCode(), fieldValue2.hashCode());
    }

    @Test
    void hashCode_should_be_different_for_different_objects() {
        StatutoryTimeFrame24WeeksFieldValue fieldValue1 = new StatutoryTimeFrame24WeeksFieldValue(
            YesOrNo.YES,
            "Test reason",
            "Test user",
            "2024-01-01T10:00:00Z"
        );

        StatutoryTimeFrame24WeeksFieldValue fieldValue2 = new StatutoryTimeFrame24WeeksFieldValue(
            YesOrNo.NO,
            "Test reason",
            "Test user",
            "2024-01-01T10:00:00Z"
        );

        assertNotEquals(fieldValue1.hashCode(), fieldValue2.hashCode());
    }
}
