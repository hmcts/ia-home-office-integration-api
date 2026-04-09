package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd;

import static junit.framework.TestCase.assertEquals;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.RequiredFieldMissingException;

public class CaseDetailsTest {

    private final long id = 123L;
    private final String jurisdiction = "IA";
    private final String caseTypeId = "Asylum";
    private final State state = State.APPEAL_STARTED;
    private final CaseData caseData = mock(CaseData.class);
    private final LocalDateTime createdDate = LocalDateTime.parse("2019-01-31T11:22:33");
    private final LocalDateTime lastModified = LocalDateTime.parse("2019-02-01T12:23:34");
    private final Integer lockedBy = 456;
    private final Integer securityLevel = 1;
    private final Classification securityClassification = Classification.PUBLIC;
    private final String callbackResponseStatus = "SUCCESS";
    private final Integer version = 1;

    private CaseDetails<CaseData> caseDetails = new CaseDetails<>(
        id,
        jurisdiction,
        caseTypeId,
        state,
        caseData,
        createdDate,
        lastModified,
        lockedBy,
        securityLevel,
        securityClassification,
        callbackResponseStatus,
        version
    );

    @Test
    public void should_hold_onto_values() {

        assertEquals(id, caseDetails.getId());
        assertEquals(jurisdiction, caseDetails.getJurisdiction());
        assertEquals(caseTypeId, caseDetails.getCaseTypeId());
        assertEquals(state, caseDetails.getState());
        assertEquals(caseData, caseDetails.getCaseData());
        assertEquals(createdDate, caseDetails.getCreatedDate());
        assertEquals(lastModified, caseDetails.getLastModified());
        assertEquals(lockedBy, caseDetails.getLockedBy());
        assertEquals(securityLevel, caseDetails.getSecurityLevel());
        assertEquals(securityClassification, caseDetails.getSecurityClassification());
        assertEquals(callbackResponseStatus, caseDetails.getCallbackResponseStatus());
        assertEquals(version, caseDetails.getVersion());
    }

    @Test
    public void should_throw_required_field_missing_exception() {

        CaseDetails<CaseData> caseDetails = new CaseDetails<>(
            id,
            null,
            caseTypeId,
            null,
            null,
            null,
            lastModified,
            lockedBy,
            securityLevel,
            securityClassification,
            callbackResponseStatus,
            version
        );

        assertThatThrownBy(caseDetails::getJurisdiction)
            .isExactlyInstanceOf(RequiredFieldMissingException.class)
            .hasMessageContaining("jurisdiction");

        assertThatThrownBy(caseDetails::getState)
            .isExactlyInstanceOf(RequiredFieldMissingException.class)
            .hasMessageContaining("state");

        assertThatThrownBy(caseDetails::getCaseData)
            .isExactlyInstanceOf(RequiredFieldMissingException.class)
            .hasMessageContaining("caseData");

        assertThatThrownBy(caseDetails::getCreatedDate)
            .isExactlyInstanceOf(RequiredFieldMissingException.class)
            .hasMessageContaining("createdDate");
    }
}
