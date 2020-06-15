package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class HomeOfficeInstructTest {
    private HomeOfficeInstruct homeOfficeInstruct;

    @BeforeEach
    void setUp() {
        homeOfficeInstruct = new HomeOfficeInstruct(
            Mockito.mock(ConsumerReference.class),
            Mockito.mock(CourtOutcome.class),
            "some-ho-reference",
            Mockito.mock(MessageHeader.class),
            "some-message-type"
        );
    }

    @Test
    public void has_correct_values_after_setting() {
        assertNotNull(homeOfficeInstruct.getConsumerReference());
        assertNotNull(homeOfficeInstruct.getCourtOutcome());
        assertNotNull(homeOfficeInstruct.getMessageHeader());
        assertEquals("some-ho-reference", homeOfficeInstruct.getHoReference());
        assertEquals("some-message-type", homeOfficeInstruct.getMessageType());
    }
}
