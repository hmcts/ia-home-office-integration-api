package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

@SuppressWarnings("unchecked")
class NationalityFieldValueTest {

    private NationalityFieldValue nationalityFieldValue;

    @Test
    void has_correct_values() {

        nationalityFieldValue = new NationalityFieldValue("ZZ");
        assertEquals("ZZ", nationalityFieldValue.getCode());
    }
}
