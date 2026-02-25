package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain;

public class CaseNotFoundException extends RuntimeException {
    public CaseNotFoundException(String message) {
        super(message);
    }
}
