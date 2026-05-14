package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CaseGoneExceptionTest {

    @Test
    void shouldCreateExceptionWithMessage() {
        // Given
        String message = "Case no longer exists for caseId: 12345";

        // When
        CaseGoneException exception = new CaseGoneException(message);

        // Then
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }
}
