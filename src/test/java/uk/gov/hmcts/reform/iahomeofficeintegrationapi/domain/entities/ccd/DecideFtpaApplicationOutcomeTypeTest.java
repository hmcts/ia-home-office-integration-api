package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class DecideFtpaApplicationOutcomeTypeTest {

    @Test
    void testGetId() {
        assertThat(DecideFtpaApplicationOutcomeType.PARTIALLY_GRANTED.getId()).isEqualTo("partiallyGranted");
    }

    @Test
    void testToString() {
        assertThat(DecideFtpaApplicationOutcomeType.PARTIALLY_GRANTED.toString()).hasToString("partiallyGranted");
    }

    @Test
    void testFrom_ValidId() {
        assertThat(DecideFtpaApplicationOutcomeType.from("partiallyGranted")).isEqualTo(DecideFtpaApplicationOutcomeType.PARTIALLY_GRANTED);
    }

    @Test
    void testFrom_InvalidId() {
        assertThatThrownBy(() -> DecideFtpaApplicationOutcomeType.from("invalidId"))
                .isExactlyInstanceOf(IllegalArgumentException.class);
    }
}