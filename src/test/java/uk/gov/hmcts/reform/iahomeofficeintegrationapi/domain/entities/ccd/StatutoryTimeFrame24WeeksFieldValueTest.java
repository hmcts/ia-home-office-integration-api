package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

@SuppressWarnings("unchecked")
class StatutoryTimeFrame24WeeksFieldValueTest {

    private StatutoryTimeFrame24WeeksFieldValue statutoryTimeFrame24WeeksFieldValue;

    @Test
    void has_correct_values() {

        statutoryTimeFrame24WeeksFieldValue = new StatutoryTimeFrame24WeeksFieldValue("YYZ");
        assertEquals("YYZ", statutoryTimeFrame24WeeksFieldValue.getDavidToFixThisForMeMerciBeaucoup());
    }
}
