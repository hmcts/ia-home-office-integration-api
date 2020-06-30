package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Test
    public void date_returned_in_iac_format_for_valid_ho_date() {
        String iacDate = HomeOfficeDateFormatter.getIacDecisionDate("2017-07-21T17:32:28Z");
        assertNotNull(iacDate);
        assertThat(iacDate.length()).isEqualTo(10);
        assertThat(iacDate.charAt(2)).isEqualTo('/');
        assertThat(iacDate.charAt(5)).isEqualTo('/');
    }

    @Test
    public void date_not_returned_in_iac_format_invalid_ho_date() {
        String iacDate = HomeOfficeDateFormatter.getIacDecisionDate("2017-07-2117:32:28");
        assertNotNull(iacDate);
        assertEquals("", iacDate);
    }
}
