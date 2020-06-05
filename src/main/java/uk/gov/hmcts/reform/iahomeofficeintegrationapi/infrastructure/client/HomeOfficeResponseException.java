package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client;

import org.springframework.web.client.RestClientResponseException;

public class HomeOfficeResponseException extends RuntimeException {

    public HomeOfficeResponseException(String message, RestClientResponseException cause) {
        super(message, cause);
    }

    public HomeOfficeResponseException(String message) {
        super(message);
    }

}
