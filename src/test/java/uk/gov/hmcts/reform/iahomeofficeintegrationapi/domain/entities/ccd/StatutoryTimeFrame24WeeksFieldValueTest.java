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
        
        assertEquals(YesOrNo.YES, statutoryTimeFrame24WeeksFieldValue.getStf24wStatus());
        assertEquals("Test reason", statutoryTimeFrame24WeeksFieldValue.getStf24wStatusReason());
        assertEquals("Test user", statutoryTimeFrame24WeeksFieldValue.getUser());
        assertEquals("2024-01-01", statutoryTimeFrame24WeeksFieldValue.getDateAdded());
    }
}
