package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.STATUTORY_TIMEFRAME_24WEEKS_STATUSES;

class CaseDataContentTest {

    private final String caseReference = "1234";
    private Map<String, Object> data;
    private Map<String, Object> event;
    private final String eventToken = "eventToken";
    private final boolean ignoreWarning = true;

    private CaseDataContent caseDataContent;

    @Test
    void should_test_equals_contract() {

        EqualsVerifier.simple()
            .forClass(CaseDataContent.class)
            .verify();
    }

    @Test
    void should_hold_onto_values() {

        data = new HashMap<>();
        data.put(STATUTORY_TIMEFRAME_24WEEKS_STATUSES.value(), "True");

        event = new HashMap<>();
        event.put("id", Event.SET_HOME_OFFICE_STATUTORY_TIMEFRAME_STATUS.toString());

        caseDataContent =
            new CaseDataContent(caseReference, data, event, eventToken, ignoreWarning);

        assertEquals("1234", caseDataContent.getCaseReference());
        assertEquals(data, caseDataContent.getData());
        assertEquals(Event.SET_HOME_OFFICE_STATUTORY_TIMEFRAME_STATUS.toString(), caseDataContent.getEvent().get("id"));
        assertEquals("eventToken", caseDataContent.getEventToken());
        assertEquals(true, caseDataContent.isIgnoreWarning());
    }
}
