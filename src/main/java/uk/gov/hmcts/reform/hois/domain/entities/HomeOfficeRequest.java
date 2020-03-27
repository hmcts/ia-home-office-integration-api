package uk.gov.hmcts.reform.hois.domain.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
public class HomeOfficeRequest {
    private String iaHomeOfficeReference;
    @JsonProperty("messageHeader")
    private transient MessageHeader messageHeader;
    @JsonProperty("searchParams")
    private transient SearchParameters[] searchParameters;

    public String getIaHomeOfficeReference() {
        return iaHomeOfficeReference;
    }

    public void setIaHomeOfficeReference(String iaHomeOfficeReference) {
        this.iaHomeOfficeReference = iaHomeOfficeReference;
    }

}

