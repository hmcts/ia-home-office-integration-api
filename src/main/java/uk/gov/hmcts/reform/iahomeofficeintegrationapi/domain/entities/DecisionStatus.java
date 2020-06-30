package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import static java.util.Objects.requireNonNull;

public class DecisionStatus {

    private CodeWithDescription decisionType;
    private String decisionDate;

    private DecisionStatus() {

    }

    public DecisionStatus(CodeWithDescription decisionType, String decisionDate) {
        this.decisionType = decisionType;
        this.decisionDate = decisionDate;
    }

    public CodeWithDescription getDecisionType() {
        requireNonNull(decisionType);
        return decisionType;
    }

    public String getDecisionDate() {
        requireNonNull(decisionDate);
        return decisionDate;
    }
}
