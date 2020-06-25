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
public class ConsumerReferenceTest {

    @Mock
    private ConsumerInstruct consumerInstruct;

    private ConsumerReference consumerReference;

    @BeforeEach
    void setUp() {
        consumerReference = new ConsumerReference(consumerInstruct);
    }

    @Test
    public void has_correct_values() {
        assertNotNull(consumerReference);
        assertEquals(consumerInstruct, consumerReference.getConsumerInstruct());

    }
}
