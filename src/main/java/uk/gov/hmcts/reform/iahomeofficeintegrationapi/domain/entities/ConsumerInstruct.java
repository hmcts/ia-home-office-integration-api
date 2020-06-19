package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConsumerInstruct {

    private String code;
    @JsonProperty("consumer")
    private ConsumerType consumerType;
    private String description;
    private String value;

    public ConsumerInstruct(String code, ConsumerType consumerType, String description, String value) {
        this.code = code;
        this.consumerType = consumerType;
        this.description = description;
        this.value = value;
    }

    private ConsumerInstruct() {
    }

    public String getCode() {
        requireNonNull(code);
        return code;
    }

    public ConsumerType getConsumerType() {
        return consumerType;
    }

    public String getDescription() {
        return description;
    }

    public String getValue() {
        return value;
    }
}
