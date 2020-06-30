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
public class PersonTest {
    @Mock
    CodeWithDescription mockCode;

    private Person person;

    @BeforeEach
    void setUp() {
        person = new Person(
          mockCode,
          mockCode,
          "firstName",
          "surName",
          "firstName-surName"
        );
    }

    @Test
    public void has_correct_values_after_setting() {
        assertNotNull(person);
        assertNotNull(person.getNationality());
        assertNotNull(person.getGender());
        assertEquals("firstName", person.getGivenName());
        assertEquals("surName", person.getFamilyName());
        assertEquals("firstName-surName", person.getFullName());
    }
}
