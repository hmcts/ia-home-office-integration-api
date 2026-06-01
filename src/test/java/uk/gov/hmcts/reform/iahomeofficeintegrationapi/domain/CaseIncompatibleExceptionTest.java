package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain;

import org.junit.jupiter.api.Test;

import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.YesOrNo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CaseIncompatibleExceptionTest {

    @Test
    void shouldCreateExceptionWithMessage() {
        // Given
        String message = "Case incompatible with supplied 24-week status for caseId: 12345.";
        String fullMessage = "Case incompatible with supplied 24-week status for caseId: 12345.  Intended 24-week status was YES.";
        YesOrNo stf24wStatus = YesOrNo.YES;

        // When
        CaseIncompatibleException exception = new CaseIncompatibleException(message, stf24wStatus);

        // Then
        assertNotNull(exception);
        assertEquals(fullMessage, exception.getMessage());
    }
}
