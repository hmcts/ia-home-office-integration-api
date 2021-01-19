package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd;

import static java.util.Arrays.stream;

import com.fasterxml.jackson.annotation.JsonValue;


public enum FtpaDecisionOutcomeType {
    NOT_ADMITTED("notAdmitted"),
    PARTIALLY_GRANTED("partiallyGranted"),
    GRANTED("granted"),
    REFUSED("refused");

    @JsonValue
    private final String id;

    FtpaDecisionOutcomeType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return id;
    }

    public static FtpaDecisionOutcomeType from(String value) {
        return stream(values())
            .filter(v -> v.getId().equals(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(value + " not a FtpaDecisionOutcomeType"));
    }

}
