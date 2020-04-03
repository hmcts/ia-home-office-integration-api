package uk.gov.hmcts.reform.hois.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hois.domain.entities.ErrorDetail;
import uk.gov.hmcts.reform.hois.domain.entities.IAhoisResponse;

public class HoisDomainTest {

    private static final String hoUAN = "1111-2222-3333-4444";
    private final IAhoisResponse iaresponse = new IAhoisResponse();
    private final ErrorDetail errorDetail = new ErrorDetail("0", "test", true);

    @Test
    public void should_return_valid_values() {
        iaresponse.setIaHomeOfficeReference(hoUAN);
        assertEquals(hoUAN, iaresponse.getIaHomeOfficeReference());
        assertNotNull(errorDetail);
        assertEquals("0", errorDetail.getErrorCode());
        assertEquals("test", errorDetail.getMessageText());
        assertEquals(true, errorDetail.isSuccess());
    }
}
