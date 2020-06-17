package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client;

public class HomeOfficeResponseException extends RuntimeException {

    public HomeOfficeResponseException(String message) {
        super(message);
    }

}
