package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd;

import static java.util.Arrays.stream;

import com.fasterxml.jackson.annotation.JsonValue;


public enum HearingCentre {

    BIRMINGHAM("birmingham", "Birmingham"),
    BRADFORD("bradford", "Bradford"),
    COVENTRY("coventry", "Coventry Magistrates Court"),
    GLASGOW("glasgow", "Glasgow (Eagle Building)"),
    GLASGOW_TRIBUNAL_CENTRE("glasgowTribunalsCentre", "Glasgow Tribunals Centre"),
    HARMONDSWORTH("harmondsworth", "Harmondsworth"),
    HATTON_CROSS("hattonCross", "Hatton Cross"),
    MANCHESTER("manchester", "Manchester"),
    NEWPORT("newport", "Newport"),
    NORTH_SHIELDS("northShields", "North Shields"),
    NOTTINGHAM("nottingham", "Nottingham Justice Centre"),
    TAYLOR_HOUSE("taylorHouse", "Taylor House"),
    NEWCASTLE("newcastle", "Newcastle Civil & Family Courts and Tribunals Centre"),
    BELFAST("belfast", "Belfast"),
    REMOTE_HEARING("remoteHearing","Remote hearing");

    @JsonValue
    private final String id;

    private final String value;

    HearingCentre(String id, String value) {
        this.id = id;
        this.value = value;
    }

    public static HearingCentre fromId(String id) {
        return stream(values())
            .filter(v -> v.getId().equals(id))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(id + " not a valid Hearing Centre"));
    }

    public String getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
