package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class DecideFtpaApplicationOutcomeTypeTest {

    @Test
    public void testGetId() {
        assertEquals("partiallyGranted", DecideFtpaApplicationOutcomeType.PARTIALLY_GRANTED.getId());
    }

    @Test
    public void testToString() {
        assertEquals("partiallyGranted", DecideFtpaApplicationOutcomeType.PARTIALLY_GRANTED.toString());
    }

    @Test
    public void testFrom_ValidId() {
        assertEquals(DecideFtpaApplicationOutcomeType.PARTIALLY_GRANTED, DecideFtpaApplicationOutcomeType.from("partiallyGranted"));
    }

    @Test
    public void testFrom_InvalidId() {
        assertThrows(IllegalArgumentException.class, () -> DecideFtpaApplicationOutcomeType.from("invalidId"));
    }

}

