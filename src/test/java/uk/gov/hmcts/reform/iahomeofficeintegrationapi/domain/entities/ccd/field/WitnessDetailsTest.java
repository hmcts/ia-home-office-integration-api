package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.WitnessDetails;

public class WitnessDetailsTest {

    private final String witnessName = "Some Name";
    private final String witnessFamilyName = "Some family Name";
    private final String witnessPartyId = "Some witness party id";
    private WitnessDetails witnessDetails;

    @BeforeEach
    public void setUp() {
        witnessDetails = new WitnessDetails();
        witnessDetails.setWitnessName(witnessName);
        witnessDetails.setWitnessFamilyName(witnessFamilyName);
        witnessDetails.setWitnessPartyId(witnessPartyId);
    }

    @Test
    public void should_hold_onto_values() {
        witnessDetails = new WitnessDetails(witnessPartyId, witnessName, witnessFamilyName);

        Assertions.assertEquals(witnessName, witnessDetails.getWitnessName());
        Assertions.assertEquals(witnessFamilyName, witnessDetails.getWitnessFamilyName());
        Assertions.assertEquals(witnessPartyId, witnessDetails.getWitnessPartyId());
    }

    @Test
    public void should_able_to_initiate_witness_name_only(){
        witnessDetails = new WitnessDetails(witnessName);

        Assertions.assertEquals(witnessName, witnessDetails.getWitnessName());
    }

    @Test
    public void should_able_to_initiate_witness_full_name(){
        witnessDetails = new WitnessDetails(witnessName, witnessFamilyName);

        Assertions.assertEquals(witnessName, witnessDetails.getWitnessName());
        Assertions.assertEquals(witnessFamilyName, witnessDetails.getWitnessFamilyName());
    }
}
