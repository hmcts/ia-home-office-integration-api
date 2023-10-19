package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WitnessDetails {

    private String witnessPartyId;
    private String witnessName;
    private String witnessFamilyName;

    public WitnessDetails() {
    }

    public WitnessDetails(String witnessName) {
        this.witnessName = witnessName;
    }

    public WitnessDetails(String witnessName, String witnessFamilyName) {
        this.witnessName = witnessName;
        this.witnessFamilyName = witnessFamilyName;
    }
}
