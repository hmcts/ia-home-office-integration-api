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
class ConsumerReferenceTest {

    @Mock
    CodeWithDescription consumerType;
    private ConsumerReference consumerReference;

    @BeforeEach
    void setUp() {
        consumerReference = new ConsumerReference(
            "some-code",
            consumerType,
            "some-description",
            "some-value"
        );
    }

    @Test
    void has_correct_values_after_setting() {
        assertNotNull(consumerReference.getConsumer());
        assertEquals("some-code", consumerReference.getCode());
        assertEquals("some-description", consumerReference.getDescription());
        assertEquals("some-value", consumerReference.getValue());
    }
}
