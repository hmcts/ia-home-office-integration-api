package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class ConsumerTypeTest {

    private ConsumerType consumerType;

    @Test
    public void has_correct_values() {

        consumerType = new ConsumerType("HMCTS", "HM Courts and Tribunal Service");
        assertEquals("HMCTS", consumerType.getCode());
        assertEquals("HM Courts and Tribunal Service", consumerType.getDescription());

        consumerType.setCode("HMCTS_CHALLENGE_REF");
        consumerType.setDescription("HMCTS challenge reference");
        assertEquals("HMCTS_CHALLENGE_REF", consumerType.getCode());
        assertEquals("HMCTS challenge reference", consumerType.getDescription());
    }


}
