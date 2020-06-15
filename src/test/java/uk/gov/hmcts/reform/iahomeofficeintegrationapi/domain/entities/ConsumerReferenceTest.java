package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertNotNull;

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

    @Test
    public void has_correct_values() {

        consumerReference = new ConsumerReference(consumerInstruct);
        assertNotNull(consumerReference);
    }
}
