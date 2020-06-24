package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class HomeOfficeDateFormatterTest {

    @Test
    public void date_returned_in_the_home_office_request_format() {
        String nowDateTime = HomeOfficeDateFormatter.getCurrentDateTime();

        assertNotNull(nowDateTime);
        assertThat(nowDateTime.length()).isEqualTo(20);
        assertThat(nowDateTime.charAt(19)).isEqualTo('Z');
    }
}
