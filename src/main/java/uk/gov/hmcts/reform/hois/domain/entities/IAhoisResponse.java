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

//    @JsonPOJOBuilder(withPrefix = "")
//    public static class POJOBuilder {
//    }

    public String getIaHomeOfficeReference() {
        return iaHomeOfficeReference;
    }

    public void setIaHomeOfficeReference(String iaHomeOfficeReference) {
        this.iaHomeOfficeReference = iaHomeOfficeReference;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public DecisionStatus getDecisionStatus() {
        return decisionStatus;
    }

    public void setDecisionStatus(DecisionStatus decisionStatus) {
        this.decisionStatus = decisionStatus;
    }

    public ErrorDetail getErrorDetail() {
        return errorDetail;
    }

    public void setErrorDetail(ErrorDetail errorDetail) {
        this.errorDetail = errorDetail;
    }
}
