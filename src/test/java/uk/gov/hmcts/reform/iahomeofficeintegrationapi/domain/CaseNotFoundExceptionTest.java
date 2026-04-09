package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CaseNotFoundExceptionTest {

    @Test
    void shouldCreateExceptionWithMessage() {
        // Given
        String message = "Case not found for caseId: 12345";

        // When
        CaseNotFoundException exception = new CaseNotFoundException(message);

        // Then
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }
}
