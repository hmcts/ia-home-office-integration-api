package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
            "2024-01-01"
        );
        
        assertEquals(YesOrNo.YES, statutoryTimeFrame24WeeksFieldValue.getStatus());
        assertEquals("Test reason", statutoryTimeFrame24WeeksFieldValue.getReason());
        assertEquals("Test user", statutoryTimeFrame24WeeksFieldValue.getUser());
        assertEquals("2024-01-01", statutoryTimeFrame24WeeksFieldValue.getDateAdded());
    }

    @Test
    void should_create_object_with_all_fields() {
        YesOrNo status = YesOrNo.YES;
        String reason = "Test reason";
        String user = "Test user";
        String dateAdded = "2023-01-01";

        StatutoryTimeFrame24WeeksFieldValue fieldValue = new StatutoryTimeFrame24WeeksFieldValue(
            status, reason, user, dateAdded
        );

        assertEquals(status, fieldValue.getStatus());
        assertEquals(reason, fieldValue.getReason());
        assertEquals(user, fieldValue.getUser());
        assertEquals(dateAdded, fieldValue.getDateAdded());
    }
}
