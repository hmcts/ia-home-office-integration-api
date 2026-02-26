package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

public class HomeOfficeErrorResponse {

    private MessageHeader messageHeader;

    private HomeOfficeError errorDetail;

    private HomeOfficeErrorResponse() {
    }

    public HomeOfficeErrorResponse(MessageHeader messageHeader, HomeOfficeError errorDetail) {
        this.messageHeader = messageHeader;
        this.errorDetail = errorDetail;
    }

    public MessageHeader getMessageHeader() {
        return messageHeader;
    }

    public HomeOfficeError getErrorDetail() {
        return errorDetail;
    }
}
