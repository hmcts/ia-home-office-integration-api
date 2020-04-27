package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Event {

    @JsonEnumDefaultValue
    UNKNOWN("unknown");

    @JsonValue
    private final String id;

    Event(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }

}
