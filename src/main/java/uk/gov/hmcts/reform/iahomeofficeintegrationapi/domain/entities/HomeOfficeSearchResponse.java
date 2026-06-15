package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import java.util.Collections;
import java.util.List;

public class HomeOfficeSearchResponse {

    private final MessageHeader messageHeader;
    private final String messageType;
    private final List<HomeOfficeCaseStatus> status;
    private final HomeOfficeError errorDetail;

    private HomeOfficeSearchResponse() {
        this.messageHeader = null;
        this.messageType = null;
        this.status = Collections.emptyList();
        this.errorDetail = null;
    }

    public HomeOfficeSearchResponse(MessageHeader messageHeader,
                                    String messageType,
                                    List<HomeOfficeCaseStatus> status,
                                    HomeOfficeError errorDetail) {
        this.messageHeader = messageHeader;
        this.messageType = messageType;
        this.status = status;
        this.errorDetail = errorDetail;
    }

    public MessageHeader getMessageHeader() {
        return messageHeader;
    }

    public String getMessageType() {
        return messageType;
    }

    public List<HomeOfficeCaseStatus> getStatus() {
        return status != null ? Collections.unmodifiableList(status) : Collections.emptyList();
    }

    public HomeOfficeError getErrorDetail() {
        return errorDetail;
    }
}
