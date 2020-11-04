package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class CourtOutcomeTest {
    private CourtOutcome courtOutcome;

    @BeforeEach
    void setUp() {
        courtOutcome = new CourtOutcome(CourtType.FIRST_TIER, Outcome.ALLOWED);
    }

    @Test
    void has_correct_values_after_setting() {
        assertEquals(CourtType.FIRST_TIER, courtOutcome.getCourtType());
        assertEquals(Outcome.ALLOWED, courtOutcome.getOutcome());
    }

    @Test
    void throws_error_if_values_are_not_set() {
        courtOutcome = new CourtOutcome(null, null);

        assertThatThrownBy(() -> courtOutcome.getCourtType())
            .isExactlyInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> courtOutcome.getOutcome())
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
