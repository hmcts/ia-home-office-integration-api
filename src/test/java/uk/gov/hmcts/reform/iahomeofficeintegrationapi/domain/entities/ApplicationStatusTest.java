package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class ApplicationStatusTest {
    @Mock
    CodeWithDescription mockCode;
    @Mock
    DecisionCommunication decisionCommunication;
    private ApplicationStatus applicationStatus;

    @BeforeEach
    void setUp() {
        applicationStatus = new ApplicationStatus(
            mockCode,
            mockCode,
            decisionCommunication,
            "some-date",
            mockCode,
            "some-doc-ref",
            mockCode,
            mockCode,
            new ArrayList<HomeOfficeMetadata>(),
            new ArrayList<RejectionReason>()
        );
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

    }
}
