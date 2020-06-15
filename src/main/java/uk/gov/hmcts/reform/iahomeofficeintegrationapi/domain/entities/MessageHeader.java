package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class MessageHeader {

    @JsonProperty("consumer")
    private ConsumerType consumerType;
    @JsonProperty("correlationId")
    private String correlationId;
    @JsonProperty("eventDateTime")
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
