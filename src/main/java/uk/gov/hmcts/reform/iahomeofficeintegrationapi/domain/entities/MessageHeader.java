package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageHeader {

    @JsonProperty("consumer")
    private ConsumerType consumerType;
    private String correlationId;
    private String eventDateTime;

    private MessageHeader() {

    }

    public MessageHeader(ConsumerType consumerType, String correlationId, String eventDateTime) {
        this.consumerType = consumerType;
        this.correlationId = correlationId;
        this.eventDateTime = eventDateTime;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getEventDateTime() {
        return eventDateTime;
    }

    public ConsumerType getConsumerType() {
        return consumerType;
    }

}
