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
public class ApplicationStatusTest {
    @Mock
    CodeWithDescription decisionType;
    private ApplicationStatus applicationStatus;

    @BeforeEach
    void setUp() {
        applicationStatus = new ApplicationStatus(
            decisionType,
            "some-date"
        );
    }

    @Test
    public void has_correct_values_after_setting() {
        assertNotNull(applicationStatus.getDecisionType());
        assertEquals(decisionType, applicationStatus.getDecisionType());
        assertEquals("some-date", applicationStatus.getDecisionDate());

    }
}
