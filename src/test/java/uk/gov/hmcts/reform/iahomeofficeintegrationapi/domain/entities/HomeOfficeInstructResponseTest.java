package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class HomeOfficeInstructResponseTest {
    @Mock
    MessageHeader messageHeader;
    private HomeOfficeInstructResponse instructResponse;

    @BeforeEach
    void setUp() {
        instructResponse = new HomeOfficeInstructResponse(
            messageHeader, null
        );
    }

    @Test
    public void has_correct_values_after_setting() {
        assertNotNull(instructResponse);
        assertNotNull(instructResponse.getMessageHeader());
        assertEquals(messageHeader, instructResponse.getMessageHeader());
        assertNull(instructResponse.getErrorDetail());
    }
}
