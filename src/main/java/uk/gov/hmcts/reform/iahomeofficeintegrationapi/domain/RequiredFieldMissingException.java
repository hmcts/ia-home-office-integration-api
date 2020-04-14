package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain;

public class RequiredFieldMissingException extends RuntimeException {
    public RequiredFieldMissingException(String message) {
        super(message);
    }
}
