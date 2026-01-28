package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.YesOrNo;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
class StatutoryTimeFrame24WeeksFieldValueTest {

    private StatutoryTimeframe24Weeks statutoryTimeFrame24WeeksFieldValue;

    @Test
    void has_correct_values() {
        List<IdValue<StatutoryTimeframe24WeeksHistory>> historyList = new ArrayList<>();
        historyList.add(new IdValue<>("1", new StatutoryTimeframe24WeeksHistory(
            YesOrNo.YES,
            "Test reason",
            "Test user",
            "2024-01-01T10:00:00Z"
        )));

        statutoryTimeFrame24WeeksFieldValue = new StatutoryTimeframe24Weeks(
            historyList
        );
        
        assertNotNull(statutoryTimeFrame24WeeksFieldValue.getHistory());
        assertEquals(1, statutoryTimeFrame24WeeksFieldValue.getHistory().size());
        
        StatutoryTimeframe24WeeksHistory history = statutoryTimeFrame24WeeksFieldValue.getHistory().get(0).getValue();
        assertEquals("Test reason", history.getReason());
        assertEquals("Test user", history.getUser());
        assertEquals("2024-01-01T10:00:00Z", history.getDateTimeAdded());
    }

    @Test
    void should_create_object_with_all_fields() {
        YesOrNo status = YesOrNo.YES;
        String reason = "Test reason";

        String user = "Test user";
        String dateAdded = "2023-01-01T10:00:00Z";

        List<IdValue<StatutoryTimeframe24WeeksHistory>> historyList = new ArrayList<>();
        historyList.add(new IdValue<>("1", new StatutoryTimeframe24WeeksHistory(
            status, reason, user, dateAdded
        )));

        StatutoryTimeframe24Weeks fieldValue = new StatutoryTimeframe24Weeks(historyList
        );

        assertNotNull(fieldValue.getHistory());
        assertEquals(1, fieldValue.getHistory().size());
        
        StatutoryTimeframe24WeeksHistory history = fieldValue.getHistory().get(0).getValue();
        assertEquals(status, history.getStatus());
        assertEquals(reason, history.getReason());
        assertEquals(user, history.getUser());
        assertEquals(dateAdded, history.getDateTimeAdded());
    }
}
