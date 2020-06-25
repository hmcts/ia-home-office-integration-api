package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HomeOfficeErrorTest {

    private HomeOfficeError error;

    @BeforeEach
    void setUp() {
        error = new HomeOfficeError("1100", "some-error",false);
    }

    @Test
    public void has_correct_values_after_setting() {
        assertNotNull(error);
        assertEquals("1100", error.getErrorCode());
        assertEquals("some-error", error.getMessageText());
        assertFalse(error.isSuccess());
    }
}
