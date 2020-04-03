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
public class Status {
    private DecisionStatus decisionStatus;
    private Person person;

    public DecisionStatus getDecisionStatus() {
        return decisionStatus;
    }

    public Person getPerson() {
        return person;
    }
}
