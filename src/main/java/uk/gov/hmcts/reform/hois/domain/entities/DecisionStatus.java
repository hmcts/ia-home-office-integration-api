package uk.gov.hmcts.reform.hois.domain.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
public class DecisionStatus {
    private boolean appealable;

    @JsonProperty("applicationType")
    private FieldType applicationType;

    @JsonProperty("claimReasonType")
    private FieldType claimReasonType;

    @JsonProperty("decisionCommunication")
    private DecisionCommunication decisionCommunication;

    private String decisionDate;

    @JsonProperty("decisionType")
    private FieldType decisionType;

    public boolean isAppealable() {
        return appealable;
    }

    public void setAppealable(boolean appealable) {
        this.appealable = appealable;
    }

    public FieldType getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(FieldType applicationType) {
        this.applicationType = applicationType;
    }

    public FieldType getClaimReasonType() {
        return claimReasonType;
    }

    public void setClaimReasonType(FieldType claimReasonType) {
        this.claimReasonType = claimReasonType;
    }

    public DecisionCommunication getDecisionCommunication() {
        return decisionCommunication;
    }

    public void setDecisionCommunication(DecisionCommunication decisionCommunication) {
        this.decisionCommunication = decisionCommunication;
    }

    public String getDecisionDate() {
        return decisionDate;
    }

    public void setDecisionDate(String decisionDate) {
        this.decisionDate = decisionDate;
    }

    public FieldType getDecisionType() {
        return decisionType;
    }

    public void setDecisionType(FieldType decisionType) {
        this.decisionType = decisionType;
    }
}
