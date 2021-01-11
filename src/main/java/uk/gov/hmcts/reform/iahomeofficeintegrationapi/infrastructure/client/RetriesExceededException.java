package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client;

public class RetriesExceededException extends RuntimeException {

    public RetriesExceededException(String message, Throwable cause) {
        super(message, cause);
    }

    public RetriesExceededException(String message) {
        super(message);
    }
}
