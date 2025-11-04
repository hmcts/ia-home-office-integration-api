package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security.idam;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class IdentityManagerResponseExceptionTest {

    @Test
    void should_create_exception_with_message_and_cause() {
        String message = "Test error message";
        Throwable cause = new RuntimeException("Root cause");

        IdentityManagerResponseException exception = new IdentityManagerResponseException(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}
