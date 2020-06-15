package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class HomeOfficeInstruct {

    @JsonProperty("consumerReference")
    private ConsumerReference consumerReference;
    @JsonProperty("courtOutcome")
    private CourtOutcome courtOutcome;
    @JsonProperty("hoReference")
    private String hoReference;
    @JsonProperty("messageHeader")
    private MessageHeader messageHeader;
    @JsonProperty("messageType")
    private String messageType;

    private HomeOfficeInstruct() {
    }

    public HomeOfficeInstruct(ConsumerReference consumerReference, CourtOutcome courtOutcome, String hoReference,
                              MessageHeader messageHeader, String messageType) {
        this.consumerReference = consumerReference;
        this.courtOutcome = courtOutcome;
        this.hoReference = hoReference;
        this.messageHeader = messageHeader;
        this.messageType = messageType;
    }

    public ConsumerReference getConsumerReference() {
        return consumerReference;
    }

    public CourtOutcome getCourtOutcome() {
        return courtOutcome;
    }

    public String getHoReference() {
        return hoReference;
    }

    public MessageHeader getMessageHeader() {
        return messageHeader;
    }

    public String getMessageType() {
        return messageType;
    }

}
