package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import static java.util.Objects.requireNonNull;

public class SearchStatus {

    private Person person;
    private DecisionStatus decisionStatus;

    public SearchStatus(Person person, DecisionStatus decisionStatus) {
        this.person = person;
        this.decisionStatus = decisionStatus;
    }

    private SearchStatus() {

    }

    public Person getPerson() {
        requireNonNull(person);
        return person;
    }

    public DecisionStatus getDecisionStatus() {
        requireNonNull(decisionStatus);
        return decisionStatus;
    }
}
