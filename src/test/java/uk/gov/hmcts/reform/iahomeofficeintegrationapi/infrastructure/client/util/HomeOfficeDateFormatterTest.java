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
    void date_returned_in_the_home_office_request_format() {
        String nowDateTime = HomeOfficeDateFormatter.getCurrentDateTime();

        assertNotNull(nowDateTime);
        assertThat(nowDateTime.length()).isEqualTo(20);
        assertThat(nowDateTime.charAt(19)).isEqualTo('Z');
    }

    @Test
    void date_returned_in_iac_format_for_valid_ho_date_time() {
        String iacDate = HomeOfficeDateFormatter.getIacDateTime("2017-07-21T17:32:28Z");
        assertNotNull(iacDate);
        assertEquals("21 Jul 2017", iacDate);
    }

    @Test
    void date_returned_asis_for_invalid_ho_date_time() {
        String iacDate = HomeOfficeDateFormatter.getIacDateTime("2017-07-2117:32:28");
        assertNotNull(iacDate);
        assertEquals("2017-07-2117:32:28", iacDate);
    }

    @Test
    void date_returned_in_iac_format_for_valid_ho_date() {
        String iacDate = HomeOfficeDateFormatter.getIacDateTime("2017-07-21");
        assertNotNull(iacDate);
        assertEquals("21 Jul 2017", iacDate);
    }

    @Test
    void date_returned_asis_for_invalid_ho_date() {
        String iacDate = HomeOfficeDateFormatter.getIacDateTime("21-7-2017");
        assertNotNull(iacDate);
        assertEquals("21-7-2017", iacDate);
    }

    @Test
    void date_returned_asis_for_null_ho_date() {
        String iacDate = HomeOfficeDateFormatter.getIacDateTime(null);
        assertNull(iacDate);
    }

    @Test
    void date_of_birth_returned_in_iac_format_for_valid_date() {
        String iacDate = HomeOfficeDateFormatter.getPersonDateOfBirth(29, 2, 2000);
        assertNotNull(iacDate);
        assertEquals("29 Feb 2000", iacDate);
    }

    @Test
    void date_of_birth_returned_asis_for_invalid_date() {
        String iacDate = HomeOfficeDateFormatter.getPersonDateOfBirth(21, 0, 1970);
        assertNotNull(iacDate);
        assertEquals("21/00/1970", iacDate);
    }

    @Test
    void date_of_birth_returned_asis_for_zero_date() {
        String iacDate = HomeOfficeDateFormatter.getPersonDateOfBirth(0, 0, 0);
        assertNotNull(iacDate);
        assertEquals("00/00/0", iacDate);
    }

    @Test
    void date_returned_in_iac_format_for_valid_ho_date_no_time() {
        String iacDate = HomeOfficeDateFormatter.getIacDateAndTime("2017-07-21");
        assertNotNull(iacDate);
        assertEquals("2017-07-21T00:00:00Z", iacDate);
    }

    @Test
    void date_returned_in_iac_format_for_valid_ho_date_and_time() {
        String iacDate = HomeOfficeDateFormatter.getIacDateAndTime("2017-07-21T02:10:00Z");
        assertNotNull(iacDate);
        assertEquals("2017-07-21T02:10:00Z", iacDate);
    }
}
