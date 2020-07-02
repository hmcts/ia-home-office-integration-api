package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import static java.util.Objects.requireNonNull;

public class SearchStatus {

    private Person person;
    private ApplicationStatus applicationStatus;

    public SearchStatus(Person person, ApplicationStatus applicationStatus) {
        this.person = person;
        this.applicationStatus = applicationStatus;
    }

    private SearchStatus() {

    }

    public Person getPerson() {
        requireNonNull(person);
        return person;
    }

    public ApplicationStatus getApplicationStatus() {
        requireNonNull(applicationStatus);
        return applicationStatus;
    }
}
