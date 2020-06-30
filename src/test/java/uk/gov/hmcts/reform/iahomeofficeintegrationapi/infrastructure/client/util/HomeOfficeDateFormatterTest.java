package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

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
        assertEquals("21/07/2017", iacDate);
    }

    @Test
    public void date_returned_asis_for_invalid_ho_date() {
        String iacDate = HomeOfficeDateFormatter.getIacDecisionDate("2017-07-2117:32:28");
        assertNotNull(iacDate);
        assertEquals("2017-07-2117:32:28", iacDate);
    }

    @Test
    public void date_returned_asis_for_null_ho_date() {
        String iacDate = HomeOfficeDateFormatter.getIacDecisionDate(null);
        assertNull(iacDate);
    }
}
