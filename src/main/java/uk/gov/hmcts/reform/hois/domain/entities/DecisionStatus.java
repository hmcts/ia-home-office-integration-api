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

}
