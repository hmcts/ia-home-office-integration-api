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
class HomeOfficeInstructResponseTest {
    @Mock
    MessageHeader messageHeader;
    @Mock
    private HomeOfficeError errorDetail;
    private HomeOfficeInstructResponse instructResponse;

    @BeforeEach
    void setUp() {
        instructResponse = new HomeOfficeInstructResponse(
            messageHeader, errorDetail
        );
    }

    @Test
    void has_correct_values_after_setting() {
        assertNotNull(instructResponse);
        assertNotNull(instructResponse.getMessageHeader());
        assertEquals(messageHeader, instructResponse.getMessageHeader());
        assertEquals(errorDetail, instructResponse.getErrorDetail());
    }
}
