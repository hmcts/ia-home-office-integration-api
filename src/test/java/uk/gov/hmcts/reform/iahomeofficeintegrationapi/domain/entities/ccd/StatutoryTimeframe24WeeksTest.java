package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.YesOrNo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class StatutoryTimeframe24WeeksTest {

    @Test
    void shouldTestEqualsAndHashCode() {
        // Given
        StatutoryTimeframe24Weeks timeframe1 = new StatutoryTimeframe24Weeks(
            YesOrNo.YES,
            "Reason 1",
            "User 1",
            "2023-12-01T00:00:00Z"
        );

        StatutoryTimeframe24Weeks timeframe2 = new StatutoryTimeframe24Weeks(
            YesOrNo.YES,
            "Reason 1",
            "User 1",
            "2023-12-01T00:00:00Z"
        );

        StatutoryTimeframe24Weeks timeframe3 = new StatutoryTimeframe24Weeks(
            YesOrNo.NO,
            "Reason 2",
            "User 2",
            "2023-12-02T00:00:00Z"
        );

        // Then
        assertEquals(timeframe1, timeframe2);
        assertEquals(timeframe1.hashCode(), timeframe2.hashCode());
        
        assertNotEquals(timeframe1, timeframe3);
        assertNotEquals(timeframe1.hashCode(), timeframe3.hashCode());
    }
}
