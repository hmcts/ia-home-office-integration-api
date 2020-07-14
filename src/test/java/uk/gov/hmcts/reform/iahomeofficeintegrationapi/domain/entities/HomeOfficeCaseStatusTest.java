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
public class HomeOfficeCaseStatusTest {
    @Mock
    Person person;
    @Mock
    ApplicationStatus applicationStatus;

    private HomeOfficeCaseStatus homeOfficeCaseStatus;

    @BeforeEach
    void setUp() {
        homeOfficeCaseStatus = new HomeOfficeCaseStatus(
            person, applicationStatus);
    }

    @Test
    public void has_correct_values_after_setting() {
        assertNotNull(homeOfficeCaseStatus);
        assertNotNull(homeOfficeCaseStatus.getPerson());
        assertNotNull(homeOfficeCaseStatus.getApplicationStatus());
        assertEquals(person, homeOfficeCaseStatus.getPerson());
        assertEquals(applicationStatus, homeOfficeCaseStatus.getApplicationStatus());
    }
}
