package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client;

public class HomeOfficeResponseException extends RuntimeException {

    private final String errorCode;

    public HomeOfficeResponseException(final String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public HomeOfficeResponseException(String message) {
        super(message);
        errorCode = null;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
