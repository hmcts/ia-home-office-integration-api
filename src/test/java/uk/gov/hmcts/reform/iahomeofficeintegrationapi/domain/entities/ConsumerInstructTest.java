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
public class ConsumerInstructTest {
    private ConsumerInstruct consumerInstruct;
    @Mock ConsumerType consumerType;

    @BeforeEach
    void setUp() {
        consumerInstruct = new ConsumerInstruct(
            "some-code",
            consumerType,
            "some-description",
            "some-value"
        );
    }

    @Test
    public void has_correct_values_after_setting() {
        assertNotNull(consumerInstruct.getConsumerType());
        assertEquals("some-code", consumerInstruct.getCode());
        assertEquals("some-description", consumerInstruct.getDescription());
        assertEquals("some-value", consumerInstruct.getValue());
    }
}
