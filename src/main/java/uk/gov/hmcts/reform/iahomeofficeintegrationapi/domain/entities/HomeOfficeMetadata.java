package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.config.JacksonConfiguration;

public class HomeOfficeMetadata {

    private String code;
    @JsonDeserialize(using = JacksonConfiguration.BooleanStringDeserializer.class)
    private String valueBoolean;
    private String valueDateTime;
    private String valueString;

    private HomeOfficeMetadata() {
    }

    public HomeOfficeMetadata(String code, String valueBoolean, String valueDateTime, String valueString) {
        this.code = code;
        this.valueBoolean = valueBoolean;
        this.valueDateTime = valueDateTime;
        this.valueString = valueString;
    }

    public String getCode() {
        return code;
    }

    public String getValueBoolean() {
        return valueBoolean;
    }

    public String getValueDateTime() {
        return valueDateTime;
    }

    public String getValueString() {
        return valueString;
    }
}
