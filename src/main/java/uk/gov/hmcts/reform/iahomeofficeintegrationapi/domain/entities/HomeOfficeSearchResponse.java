package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import java.util.List;

public class HomeOfficeSearchResponse {

    private MessageHeader messageHeader;
    private String messageType;
    private List<SearchStatus> status;

    private HomeOfficeSearchResponse() {
    }

    public HomeOfficeSearchResponse(MessageHeader messageHeader, String messageType, List<SearchStatus> status) {
        this.messageHeader = messageHeader;
        this.messageType = messageType;
        this.status = status;
    }

    public MessageHeader getMessageHeader() {
        return messageHeader;
    }

    public String getMessageType() {
        return messageType;
    }

    public List<SearchStatus> getStatus() {
        return status;
    }
}
