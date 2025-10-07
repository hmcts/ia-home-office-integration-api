package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.roleassignment;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum Attributes {
    CASE_ID("caseId"),
    PRIMARY_LOCATION("primaryLocation"),
    JURISDICTION("jurisdiction"),
    REGION("region"),
    CASE_TYPE("caseType");

    @JsonValue
    private final String value;

    Attributes(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
