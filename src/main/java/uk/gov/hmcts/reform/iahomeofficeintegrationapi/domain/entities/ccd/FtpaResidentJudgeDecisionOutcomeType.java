package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd;

import static java.util.Arrays.stream;

import com.fasterxml.jackson.annotation.JsonValue;

public enum FtpaResidentJudgeDecisionOutcomeType {
    PARTIALLY_GRANTED("partiallyGranted"),
    GRANTED("granted"),
    REFUSED("refused"),
    REHEARD_RULE35("reheardRule35"),
    REHEARD_RULE32("reheardRule32"),
    REMADE_RULE32("remadeRule32");

    @JsonValue
    private final String id;

    FtpaResidentJudgeDecisionOutcomeType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return id;
    }

    public static FtpaResidentJudgeDecisionOutcomeType from(String id) {
        return stream(values())
            .filter(v -> v.getId().equals(id))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(id + " not a FtpaResidentJudgeDecisionOutcomeType"));
    }

}
