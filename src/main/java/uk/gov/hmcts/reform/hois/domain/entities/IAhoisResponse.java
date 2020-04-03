package uk.gov.hmcts.reform.hois.domain.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IAhoisResponse {
    private String iaHomeOfficeReference;
    @JsonProperty("person")
    private Person person;
    @JsonProperty("decisionStatus")
    private DecisionStatus decisionStatus;
    @JsonProperty("errorDetail")
    private ErrorDetail errorDetail;

    public void setIaHomeOfficeReference(String iaHomeOfficeReference) {
        this.iaHomeOfficeReference = iaHomeOfficeReference;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public void setDecisionStatus(DecisionStatus decisionStatus) {
        this.decisionStatus = decisionStatus;
    }

    public void setErrorDetail(ErrorDetail errorDetail) {
        this.errorDetail = errorDetail;
    }

    public String getIaHomeOfficeReference() {
        return iaHomeOfficeReference;
    }
}
