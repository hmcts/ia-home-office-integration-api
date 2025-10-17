package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}
