package uk.gov.hmcts.reform.hois.domain.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Arrays;
import lombok.Data;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
public class HomeOfficeResponse {
    /* @JsonProperty("ho_reference")
    private transient String hoReference;
    @JsonProperty("appeal_decision_sent_date")
    private transient String appealDecisionSentDate;

    */
    /**
     * Create a stub for now. Return a static response
     *//*
    public HomeOfficeAppealData() {
        this.hoReference = "1234-5678-6789-7890";
        this.appealDecisionSentDate = "20-02-2020";
    }

    public String getAppealDecisionSentDate() {
        return appealDecisionSentDate;
    }

    public String getHoReference() {
        return hoReference;
    }*/

    @JsonProperty("messageHeader")
    private MessageHeader messageHeader;

    private String messageType;

    @JsonProperty("status")
    private Status[] statuses;

    @JsonProperty("errorDetail")
    private ErrorDetail errorDetail;

    public MessageHeader getMessageHeader() {
        return messageHeader;
    }

    public void setMessageHeader(MessageHeader messageHeader) {
        this.messageHeader = messageHeader;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public Status[] getStatuses() {
        return Arrays.copyOf(statuses, statuses.length);
    }

    public void setStatuses(Status... statuses) {
        this.statuses = Arrays.copyOf(statuses, statuses.length);
    }

    public ErrorDetail getErrorDetail() {
        return errorDetail;
    }

    public void setErrorDetail(ErrorDetail errorDetail) {
        this.errorDetail = errorDetail;
    }
}

