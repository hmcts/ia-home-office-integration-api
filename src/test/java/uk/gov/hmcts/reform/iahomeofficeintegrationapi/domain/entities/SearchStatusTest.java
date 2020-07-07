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
public class SearchStatusTest {
    @Mock
    Person person;
    @Mock
    ApplicationStatus applicationStatus;

    private SearchStatus searchStatus;

    @BeforeEach
    void setUp() {
        searchStatus = new SearchStatus(
            person, applicationStatus);
    }

    @Test
    public void has_correct_values_after_setting() {
        assertNotNull(searchStatus);
        assertNotNull(searchStatus.getPerson());
        assertNotNull(searchStatus.getApplicationStatus());
        assertEquals(person, searchStatus.getPerson());
        assertEquals(applicationStatus, searchStatus.getApplicationStatus());
    }
}
