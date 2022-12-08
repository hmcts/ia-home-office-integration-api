package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import static java.util.Arrays.stream;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Optional;

public enum AppealType {

    REVOCATION_OF_PROTECTION("revocationOfProtection"),
    PROTECTION("protection"),
    EEA("refusalOfEu"),
    HUMAN_RIGHTS("refusalOfHumanRights"),
    DEPRIVATION_OF_CITIZENSHIP("deprivation"),
    EU("euSettlementScheme");

    @JsonValue
    private String value;

    AppealType(String value) {
        this.value = value;
    }

    public static Optional<AppealType> from(
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
