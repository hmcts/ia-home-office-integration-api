package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class HomeOfficeDateFormatterTest {

    @Test
    void date_returned_in_the_home_office_request_format() {
        String nowDateTime = HomeOfficeDateFormatter.getCurrentDateTime();

        assertNotNull(nowDateTime);
        assertThat(nowDateTime).hasSize(20);
        assertThat(nowDateTime.charAt(19)).isEqualTo('Z');
    }

    @ParameterizedTest
    @MethodSource("provideDateTimeInputs")
    void date_returned_in_expected_format_for_valid_date_time(String date, String expectedOutput) {
        String iacDate = HomeOfficeDateFormatter.getIacDateTime(date);
        assertNotNull(iacDate);
        assertEquals(expectedOutput, iacDate);
    }

    static Stream<Arguments> provideDateTimeInputs() {
        return Stream.of(
                Arguments.of("2017-07-21T17:32:28Z", "21 Jul 2017"),
                Arguments.of("2017-07-2117:32:28", "2017-07-2117:32:28"),
                Arguments.of("2017-07-21", "21 Jul 2017"),
                Arguments.of("21-7-2017", "21-7-2017")
        );
    }

    @Test
    void date_returned_asis_for_null_ho_date() {
        String iacDate = HomeOfficeDateFormatter.getIacDateTime(null);
        assertNull(iacDate);
    }

    @ParameterizedTest
    @MethodSource("provideDateOfBirthInputs")
    void date_of_birth_returned_in_expected_format(int day, int month, int year, String expectedOutput) {
        String iacDate = HomeOfficeDateFormatter.getPersonDateOfBirth(day, month, year);
        assertNotNull(iacDate);
        assertEquals(expectedOutput, iacDate);
    }

    static Stream<Arguments> provideDateOfBirthInputs() {
        return Stream.of(
                Arguments.of(29, 2, 2000, "29 Feb 2000"),
                Arguments.of(21, 0, 1970, "21/00/1970"),
                Arguments.of(0, 0, 0, "00/00/0")
        );
    }

    @ParameterizedTest
    @MethodSource("provideDateAndTimeInputs")
    void date_returned_in_iac_format_for_valid_ho_date_and_time(String date, String expectedOutput) {
        String iacDate = HomeOfficeDateFormatter.getIacDateAndTime(date);
        assertNotNull(iacDate);
        assertEquals(expectedOutput, iacDate);
    }

    static Stream<Arguments> provideDateAndTimeInputs() {
        return Stream.of(
                Arguments.of("2017-07-21", "2017-07-21T00:00:00Z"),
                Arguments.of("2017-07-21T02:10:00Z", "2017-07-21T02:10:00Z")
        );
    }
}
