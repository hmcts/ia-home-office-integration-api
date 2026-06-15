package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class DynamicListTest {

    @Test
    void should_hold_onto_values_with_string_constructor() {
        DynamicList dynamicList = new DynamicList("testValue");

        assertNotNull(dynamicList);
        assertNotNull(dynamicList.getValue());
        assertEquals("testValue", dynamicList.getValue().getCode());
        assertEquals("testValue", dynamicList.getValue().getLabel());
    }

    @Test
    void should_hold_onto_values_with_full_constructor() {
        Value value = new Value("code", "label");
        List<Value> listItems = Arrays.asList(
            new Value("code1", "label1"),
            new Value("code2", "label2")
        );

        DynamicList dynamicList = new DynamicList(value, listItems);

        assertNotNull(dynamicList);
        assertEquals(value, dynamicList.getValue());
        assertThat(dynamicList.getListItems()).hasSize(2);
    }

    @Test
    void should_return_empty_list_when_list_items_is_null() {
        Value value = new Value("code", "label");
        DynamicList dynamicList = new DynamicList(value, null);

        assertNotNull(dynamicList.getListItems());
        assertThat(dynamicList.getListItems()).isEmpty();
    }

    @Test
    void should_return_unmodifiable_list_items() {
        Value value = new Value("code", "label");
        List<Value> listItems = Arrays.asList(new Value("code1", "label1"));
        DynamicList dynamicList = new DynamicList(value, listItems);

        assertThatThrownBy(() -> dynamicList.getListItems().add(new Value("new", "new")))
            .isExactlyInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void should_return_empty_list_for_string_constructor() {
        DynamicList dynamicList = new DynamicList("testValue");

        assertNotNull(dynamicList.getListItems());
        assertThat(dynamicList.getListItems()).isEmpty();
    }
}
