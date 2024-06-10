package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ApplicationStatusTest {
    @Mock
    CodeWithDescription mockCode;
    @Mock
    DecisionCommunication decisionCommunication;
    private ApplicationStatus applicationStatus;

    @BeforeEach
    void setUp() {
        applicationStatus = new ApplicationStatus.Builder()
                .withApplicationType(mockCode)
                .withClaimReasonType(mockCode)
                .withDecisionCommunication(decisionCommunication)
                .withDecisionDate("some-date")
                .withDecisionType(mockCode)
                .withDocumentReference("some-doc-ref")
                .withRoleSubType(mockCode)
                .withRoleType(mockCode)
                .withHomeOfficeMetadata(new ArrayList<>())
                .withRejectionReasons(new ArrayList<>())
                .build();
    }

    @Test
    public void has_correct_values_after_setting() {
        assertNotNull(applicationStatus.getDecisionType());
        assertEquals(mockCode, applicationStatus.getApplicationType());
        assertEquals(mockCode, applicationStatus.getClaimReasonType());
        assertEquals(decisionCommunication, applicationStatus.getDecisionCommunication());
        assertEquals("some-date", applicationStatus.getDecisionDate());
        assertEquals(mockCode, applicationStatus.getDecisionType());
        assertEquals("some-doc-ref", applicationStatus.getDocumentReference());
        assertEquals(mockCode, applicationStatus.getRoleType());
        assertEquals(mockCode, applicationStatus.getRoleSubType());
        assertNotNull(applicationStatus.getHomeOfficeMetadata());
        assertNotNull(applicationStatus.getRejectionReasons());
        assertThat(applicationStatus.getHomeOfficeMetadata()).isEmpty();
        assertThat(applicationStatus.getRejectionReasons()).isEmpty();
        assertNull(applicationStatus.getCcdHomeOfficeMetadata());
        assertNull(applicationStatus.getCcdRejectionReasons());

    }
}
