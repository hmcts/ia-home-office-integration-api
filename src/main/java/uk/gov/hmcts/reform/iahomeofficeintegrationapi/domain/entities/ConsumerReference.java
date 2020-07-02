package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import static java.util.Objects.requireNonNull;

public class ConsumerReference {

    private String code;
    private CodeWithDescription consumer;
    private String description;
    private String value;

    public ConsumerReference(String code, CodeWithDescription consumer, String description, String value) {
        this.code = code;
        this.consumer = consumer;
        this.description = description;
        this.value = value;
    }

    private ConsumerReference() {
    }

    public String getCode() {
        requireNonNull(code);
        return code;
    }

    public CodeWithDescription getConsumer() {
        return consumer;
    }

    public String getDescription() {
        return description;
    }

    public String getValue() {
        return value;
    }

}
