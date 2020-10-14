package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd;

import static java.util.Arrays.stream;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Optional;


public enum HearingCentre {

    BIRMINGHAM("birmingham"),
    BRADFORD("bradford"),
    COVENTRY("coventry"),
    GLASGOW("glasgow"),
    GLASGOW_TRIBUNAL_CENTRE("glasgowTribunalsCentre"),
    HATTON_CROSS("hattonCross"),
    MANCHESTER("manchester"),
    NEWPORT("newport"),
    NORTH_SHIELDS("northShields"),
    NOTTINGHAM("nottingham"),
    TAYLOR_HOUSE("taylorHouse"),
    NEWCASTLE("newcastle");

    @JsonValue
    private final String value;

    HearingCentre(String value) {
        this.value = value;
    }

    public static Optional<HearingCentre> from(
        String value
    ) {
        return stream(values())
            .filter(v -> v.getValue().equals(value))
            .findFirst();
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return getValue();
    }
}
