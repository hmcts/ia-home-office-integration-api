package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class SearchParamsTest {

    private SearchParams searchParams;

    @BeforeEach
    void setUp() {
        searchParams = new SearchParams("some-type", "some-value");
    }

    @Test
    void has_correct_values() {
        assertNotNull(searchParams);
        assertEquals("some-type", searchParams.getSpType());
        assertEquals("some-value", searchParams.getSpValue());
    }


}
