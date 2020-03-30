package uk.gov.hmcts.reform.hois.domain.entities;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchParameters {
    private String spType;
    private String spValue;

    public String getSpType() {
        return spType;
    }

    public void setSpType(String spType) {
        this.spType = spType;
    }

    public String getSpValue() {
        return spValue;
    }

    public void setSpValue(String spValue) {
        this.spValue = spValue;
    }
}
