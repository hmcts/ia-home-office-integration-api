package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.YesOrNo;
import java.util.ArrayList;
import java.util.List;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.IdValue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class StatutoryTimeframe24WeeksTest {

    @Test
    void testEqualsAndHashCode() {
        List<IdValue<StatutoryTimeframe24WeeksHistory>> historyList1 = new ArrayList<>();
        historyList1.add(new IdValue<>("1", new StatutoryTimeframe24WeeksHistory(
            YesOrNo.YES,
            "Reason 1",
            "User 1",
            "2023-01-01T10:00:00"
        )));

        StatutoryTimeframe24Weeks timeframe1 = new StatutoryTimeframe24Weeks(
            YesOrNo.YES,
            historyList1
        );

        List<IdValue<StatutoryTimeframe24WeeksHistory>> historyList2 = new ArrayList<>();
        historyList2.add(new IdValue<>("1", new StatutoryTimeframe24WeeksHistory(
            YesOrNo.YES,
            "Reason 1",
            "User 1",
            "2023-01-01T10:00:00"
        )));

        StatutoryTimeframe24Weeks timeframe2 = new StatutoryTimeframe24Weeks(
            YesOrNo.YES,
            historyList2
        );

        List<IdValue<StatutoryTimeframe24WeeksHistory>> historyList3 = new ArrayList<>();
        historyList3.add(new IdValue<>("1", new StatutoryTimeframe24WeeksHistory(
            YesOrNo.NO,
            "Reason 2",
            "User 2",
            "2023-01-02T10:00:00"
        )));

        StatutoryTimeframe24Weeks timeframe3 = new StatutoryTimeframe24Weeks(
            YesOrNo.NO,
            historyList3
        );

        // Then
        assertEquals(timeframe1, timeframe2);
        assertEquals(timeframe1.hashCode(), timeframe2.hashCode());
        
        assertNotEquals(timeframe1, timeframe3);
        assertNotEquals(timeframe1.hashCode(), timeframe3.hashCode());
    }
}
