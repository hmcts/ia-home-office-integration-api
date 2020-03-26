package uk.gov.hmcts.reform.hois.domain.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class HomeOfficeRequest {
    @JsonProperty("ho_reference")
    private transient String hoReference;

    public String getHoReference() {
        return hoReference;
    }

    public void setHoReference(String hoReference) {
        this.hoReference = hoReference;
    }
}
