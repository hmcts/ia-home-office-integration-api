package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import static java.util.Objects.requireNonNull;

public class CourtOutcome {

    private CourtType courtType;
    private Outcome outcome;

    private CourtOutcome() {
    }

    public CourtOutcome(CourtType courtType, Outcome outcome) {
        this.courtType = courtType;
        this.outcome = outcome;
    }

    public CourtType getCourtType() {
        requireNonNull(courtType);
        return courtType;
    }

    public Outcome getOutcome() {
        requireNonNull(outcome);
        return outcome;
    }

}
