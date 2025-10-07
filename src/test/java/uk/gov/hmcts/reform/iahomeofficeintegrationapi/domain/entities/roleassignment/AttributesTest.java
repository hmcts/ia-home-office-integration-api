package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.roleassignment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class AttributesTest {

    @ParameterizedTest
    @EnumSource(value = Attributes.class)
    void to_string_gets_values(Attributes attributes) {
        assertEquals(attributes.toString(), attributes.getValue());
    }

    @ParameterizedTest
    @CsvSource({
        "CASE_ID,caseId",
        "PRIMARY_LOCATION,primaryLocation",
        "JURISDICTION,jurisdiction",
        "REGION,region",
        "CASE_TYPE,caseType"
    })
    void has_correct_values(Attributes attributes, String expectedValue) {
        assertEquals(expectedValue, attributes.getValue());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(5, Attributes.values().length);
    }
}
