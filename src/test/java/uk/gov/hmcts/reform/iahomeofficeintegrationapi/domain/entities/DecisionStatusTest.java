package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class DecisionStatusTest {
    @Mock
    CodeWithDescription decisionType;
    private DecisionStatus decisionStatus;

    @BeforeEach
    void setUp() {
        decisionStatus = new DecisionStatus(
            decisionType,
            "some-date"
        );
    }

    @Test
    public void has_correct_values_after_setting() {
        assertNotNull(decisionStatus.getDecisionType());
        assertEquals(decisionType, decisionStatus.getDecisionType());
        assertEquals("some-date", decisionStatus.getDecisionDate());

    }
}
