package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

public class CodeWithDescription {

    private String code;
    private String description;

    public CodeWithDescription(String code, String description) {
        this.code = code;
        this.description = description;
    }

    private CodeWithDescription() {

    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

}
