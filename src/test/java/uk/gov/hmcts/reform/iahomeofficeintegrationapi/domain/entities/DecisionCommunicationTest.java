package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DecisionCommunicationTest {
    private DecisionCommunication decisionCommunication;

    @BeforeEach
    void setUp() {

        decisionCommunication = new DecisionCommunication(
            "some-text", "some-date", "some-date", "some-type");
    }

    @Test
    void has_correct_values_after_setting() {
        assertNotNull(decisionCommunication);
        assertEquals("some-text", decisionCommunication.getDescription());
        assertEquals("some-date", decisionCommunication.getDispatchDate());
        assertEquals("some-date", decisionCommunication.getSentDate());
        assertEquals("some-type", decisionCommunication.getType());
    }


}
