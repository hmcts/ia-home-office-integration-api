package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain;

public class CaseGoneException extends RuntimeException {
    public CaseGoneException(String message) {
        super(message);
    }
}
