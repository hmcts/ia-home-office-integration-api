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
public class HomeOfficeErrorResponseTest {
    @Mock
    MessageHeader messageHeader;
    @Mock
    private HomeOfficeError errorDetail;
    private HomeOfficeErrorResponse errorResponse;

    @BeforeEach
    void setUp() {
        errorResponse = new HomeOfficeErrorResponse(
            messageHeader, errorDetail
        );
    }

    @Test
    public void has_correct_values_after_setting() {
        assertNotNull(errorResponse);
        assertNotNull(errorResponse.getMessageHeader());
        assertEquals(messageHeader, errorResponse.getMessageHeader());
        assertEquals(errorDetail, errorResponse.getErrorDetail());
    }
}
