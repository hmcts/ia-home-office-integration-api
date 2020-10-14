package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;

import java.time.format.DateTimeParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DateTimeExtractorTest {

    @Mock private DateTimeExtractor dateTimeExtractor;

    final String listCaseHearingDate = "2019-05-03T14:25:15";
    final String invalidIso8601HearingDate = "2019-05-03 14:25:15";
    final String extractedHearingDateFormatted = "2019-05-03";
    final String extractedHearingTime = "14:25:15";

    @BeforeEach
    void setUp() {
        dateTimeExtractor = new DateTimeExtractor();
    }

    @Test
    void should_throw_when_invalid_iso_8610_date() {

        assertThatThrownBy(() -> dateTimeExtractor.extractHearingDate(invalidIso8601HearingDate))
            .isExactlyInstanceOf(DateTimeParseException.class);
    }

    @Test
    void should_throw_when_invalid_iso_8610_time() {

        assertThatThrownBy(() -> dateTimeExtractor.extractHearingTime(invalidIso8601HearingDate))
            .isExactlyInstanceOf(DateTimeParseException.class);
    }

    @Test
    void should_return_extracted_date_for_valid_iso_8610_datetime() {

        String actualExtractedDate = dateTimeExtractor.extractHearingDate(listCaseHearingDate);

        assertEquals(extractedHearingDateFormatted, actualExtractedDate);
    }

    @Test
    void should_return_extracted_time_for_valid_iso_8610_datetime() {

        String actualExtractedTime = dateTimeExtractor.extractHearingTime(listCaseHearingDate);

        assertEquals(extractedHearingTime, actualExtractedTime);
    }
}
