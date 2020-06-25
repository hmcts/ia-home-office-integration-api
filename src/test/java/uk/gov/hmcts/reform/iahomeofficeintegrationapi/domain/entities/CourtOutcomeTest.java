package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class CourtOutcomeTest {
    private CourtOutcome courtOutcome;

    @BeforeEach
    void setUp() {
        courtOutcome = new CourtOutcome(CourtType.FIRST_TIER.toString(), Outcome.ALLOWED.toString());
    }

    @Test
    public void has_correct_values_after_setting() {
        assertEquals("FIRST_TIER", courtOutcome.getCourtType());
        assertEquals("ALLOWED", courtOutcome.getOutcome());
    }

    @Test
    public void throws_error_if_values_are_not_set() {
        courtOutcome = new CourtOutcome(null, null);

        assertThatThrownBy(() -> courtOutcome.getCourtType())
            .isExactlyInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> courtOutcome.getOutcome())
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
