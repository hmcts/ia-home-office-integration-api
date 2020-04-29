package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum Event {
    SUBMIT_APPEAL("submitAppeal"),
    @JsonEnumDefaultValue
    UNKNOWN("unknown");

    private final String id;

    Event(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }

}
