package uk.gov.hmcts.reform.hois.domain.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class HomeOfficeAppealData {
    @JsonProperty("ho_reference")
    private transient String hoReference;
    @JsonProperty("appeal_decision_sent_date")
    private transient String appealDecisionSentDate;

    /**
     * Create a stub for now. Return a static response
     */
    public HomeOfficeAppealData() {
        this.hoReference = "1234-5678-6789-7890";
        this.appealDecisionSentDate = "20-02-2020";
    }

    public String getAppealDecisionSentDate() {
        return appealDecisionSentDate;
    }

    public String getHoReference() {
        return hoReference;
    }
}
