package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HomeOfficeMetadataTest {

    private HomeOfficeMetadata metadata;

    @BeforeEach
    void setUp() {

        metadata = new HomeOfficeMetadata(
            "some-text", "true", "some-date", "some-text");
    }

    @Test
    void has_correct_values_after_setting() {
        assertNotNull(metadata);
        assertEquals("some-text", metadata.getCode());
        assertEquals("true", metadata.getValueBoolean());
        assertEquals("some-date", metadata.getValueDateTime());
        assertEquals("some-text", metadata.getValueString());

    }

}
