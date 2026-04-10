package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HomeOfficeMissingApplicationExceptionTest {

    @Test
    void shouldStoreMessageAndHttpStatus() {
        int status = 404;
        String message = "Application not found";

        HomeOfficeMissingApplicationException ex =
                new HomeOfficeMissingApplicationException(status, message);

        assertThat(ex.getHttpStatus()).isEqualTo(status);
        assertThat(ex.getMessage()).isEqualTo(message);
    }

    @Test
    void shouldBeRuntimeException() {
        HomeOfficeMissingApplicationException ex =
                new HomeOfficeMissingApplicationException(500, "Server error");

        assertThat(ex).isInstanceOf(RuntimeException.class);
    }
}
