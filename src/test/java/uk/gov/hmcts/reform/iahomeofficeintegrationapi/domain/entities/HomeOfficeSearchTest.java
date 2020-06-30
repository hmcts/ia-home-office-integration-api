package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class HomeOfficeSearchTest {
    @Mock
    MessageHeader messageHeader;
    @Mock
    SearchParams searchParams;

    private HomeOfficeSearch homeOfficeSearch;

    @BeforeEach
    void setUp() {
        homeOfficeSearch = new HomeOfficeSearch(
            messageHeader,
            Collections.singletonList(searchParams)
        );
    }

    @Test
    public void has_correct_values_after_setting() {
        assertNotNull(homeOfficeSearch);
        assertNotNull(homeOfficeSearch.getMessageHeader());
        assertNotNull(homeOfficeSearch.getSearchParams());
        assertThat(homeOfficeSearch.getSearchParams()).isNotEmpty();
    }
}
