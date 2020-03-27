package uk.gov.hmcts.reform.hois.domain.entities;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
public class IAhoisResponse {
    private String iaHomeOfficeReference;
    private Person person;
    private DecisionStatus decisionStatus;
    private ErrorDetail errorDetail;

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
