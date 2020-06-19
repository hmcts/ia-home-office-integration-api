package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

public class ConsumerType {

    private String code;
    private String description;

    public ConsumerType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
