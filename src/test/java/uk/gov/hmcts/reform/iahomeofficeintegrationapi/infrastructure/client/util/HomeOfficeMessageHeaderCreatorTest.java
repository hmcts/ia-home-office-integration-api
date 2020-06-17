package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class HomeOfficeMessageHeaderCreatorTest {

    @Test
    public void create_uuid_returns_valid() {
        String uuid = HomeOfficeMessageHeaderCreator.generateUuid();

        assertNotNull(uuid);
        assertEquals(36, uuid.length());
    }

}
