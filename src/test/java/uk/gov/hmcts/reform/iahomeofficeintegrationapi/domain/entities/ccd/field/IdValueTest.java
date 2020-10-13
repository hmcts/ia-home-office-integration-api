package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field;

import static junit.framework.TestCase.assertEquals;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;

public class IdValueTest {
    private final String id = "1";
    private final Integer value = 1234;

    private IdValue<Integer> addressUk = new IdValue<>(id, value);

    @Mock
    private IdValue<Integer> mockIdValue;

    @Test
    void should_hold_onto_values() {
        assertEquals(id, addressUk.getId());
        assertEquals(value, addressUk.getValue());
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> new IdValue<>(null, "some-value"))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new IdValue<>("some-id", null))
            .isExactlyInstanceOf(NullPointerException.class);

    }

}
