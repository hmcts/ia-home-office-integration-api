package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class MessageHeaderTest {
    @Mock
    private ConsumerType consumerType;
    private MessageHeader messageHeader;

    @BeforeEach
    void setUp() {
        messageHeader = new MessageHeader(
            consumerType,
            "1234-tttt-wwwwwww",
            "2020-06-15T17:35:38Z");
    }

    @Test
    public void has_correct_values() {
        assertNotNull(messageHeader.getConsumerType());
        assertEquals("1234-tttt-wwwwwww", messageHeader.getCorrelationId());
        assertEquals("2020-06-15T17:35:38Z", messageHeader.getEventDateTime());
    }

}
