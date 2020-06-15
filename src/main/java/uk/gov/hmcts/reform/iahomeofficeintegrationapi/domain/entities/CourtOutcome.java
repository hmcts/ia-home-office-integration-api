package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CourtOutcome {

    @JsonProperty("courtType")
    private String courtType;
    private String outcome;

    private CourtOutcome() {
    }

    public CourtOutcome(String courtType, String outcome) {
        this.courtType = courtType;
        this.outcome = outcome;
    }

    public String getCourtType() {
        requireNonNull(courtType);
        return courtType;
    }

    public String getOutcome() {
        requireNonNull(outcome);
        return outcome;
    }

}
