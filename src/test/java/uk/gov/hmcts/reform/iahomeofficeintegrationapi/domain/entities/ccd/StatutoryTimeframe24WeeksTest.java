package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.YesOrNo;
import java.util.ArrayList;
import java.util.List;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.IdValue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StatutoryTimeframe24WeeksTest {

    @Test
    void shouldCreateStatutoryTimeframe24WeeksWithValidData() {
        // Given
        YesOrNo status = YesOrNo.YES;
        List<IdValue<StatutoryTimeframe24WeeksHistory>> history = createHistory("1", YesOrNo.YES);

        // When
        StatutoryTimeframe24Weeks stf = new StatutoryTimeframe24Weeks(history);

        // Then
        assertNotNull(stf);
        assertEquals(1, stf.getHistory().size());
    }

    @Test
    void shouldThrowNullPointerExceptionWhenHistoryIsNull() {
        // When & Then
        assertThrows(NullPointerException.class, () -> new StatutoryTimeframe24Weeks(null));
    }

    private List<IdValue<StatutoryTimeframe24WeeksHistory>> createHistory(String id, YesOrNo status) {
        List<IdValue<StatutoryTimeframe24WeeksHistory>> history = new ArrayList<>();
        StatutoryTimeframe24WeeksHistory historyEntry = new StatutoryTimeframe24WeeksHistory(
            status,
            "Test reason",
            "Test user",
            "2024-01-01T12:00:00"
        );
        history.add(new IdValue<>(id, historyEntry));
        return history;
    }
}
