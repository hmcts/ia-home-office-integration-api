package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

public class ConsumerType {

    private String code;
    private String description;

    private ConsumerType() {
    }

    public ConsumerType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

}
