package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConsumerReference {

    @JsonProperty("consumer")
    private ConsumerInstruct consumerInstruct;

    public ConsumerReference(ConsumerInstruct consumerInstruct) {
        this.consumerInstruct = consumerInstruct;
    }

    private ConsumerReference() {

    }

    public ConsumerInstruct getConsumerInstruct() {
        return consumerInstruct;
    }

}
