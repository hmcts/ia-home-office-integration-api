package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class HomeOfficePropertiesTest {

    @Test
    void should_return_empty_map_by_default() {
        HomeOfficeProperties properties = new HomeOfficeProperties();

        assertNotNull(properties.getCodes());
        assertThat(properties.getCodes()).isEmpty();
    }

    @Test
    void should_return_unmodifiable_codes_map() {
        HomeOfficeProperties properties = new HomeOfficeProperties();

        assertThatThrownBy(() -> properties.getCodes().put("key", new HomeOfficeProperties.LookupReferenceData()))
            .isExactlyInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void should_set_codes_via_setter() {
        Map<String, HomeOfficeProperties.LookupReferenceData> codes = new HashMap<>();

        HomeOfficeProperties.LookupReferenceData lookupData = new HomeOfficeProperties.LookupReferenceData();
        lookupData.setCode("testCode");
        lookupData.setDescription("testDescription");
        codes.put("key", lookupData);

        HomeOfficeProperties properties = new HomeOfficeProperties();
        properties.setCodes(codes);

        assertThat(properties.getCodes()).hasSize(1);
        assertEquals("testCode", properties.getCodes().get("key").getCode());
        assertEquals("testDescription", properties.getCodes().get("key").getDescription());
    }

    @Test
    void should_handle_null_codes_in_setter() {
        HomeOfficeProperties properties = new HomeOfficeProperties();

        properties.setCodes(null);

        assertNotNull(properties.getCodes());
        assertThat(properties.getCodes()).isEmpty();
    }

    @Test
    void lookup_reference_data_should_hold_values() {
        HomeOfficeProperties.LookupReferenceData lookupData = new HomeOfficeProperties.LookupReferenceData();
        lookupData.setCode("code");
        lookupData.setDescription("description");

        assertEquals("code", lookupData.getCode());
        assertEquals("description", lookupData.getDescription());
    }
}
