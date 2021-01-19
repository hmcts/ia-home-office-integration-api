package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import static java.util.Arrays.stream;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Optional;

public enum EndAppealOutcome {

    STRUCK_OUT("Struck out"),
    ABANDONED("Abandoned"),
    WITHDRAWN("Withdrawn"),
    NOT_VALID("No valid appeal"),
    INCORRECT_DETAILS("Incorrect details");

    @JsonValue
    private String value;

    EndAppealOutcome(String value) {
        this.value = value;
    }

    public static Optional<EndAppealOutcome> from(
        String value
    ) {
        return stream(values())
            .filter(v -> v.getValue().equals(value))
            .findFirst();
    }

    public String getValue() {
        return value;
    }
}
