package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class SystemDateProviderTest {

    private final SystemDateProvider systemDateProvider = new SystemDateProvider();

    @Test
    void returns_now_date() {
        LocalDate actualDate = systemDateProvider.now();
        Assertions.assertNotNull(actualDate);
        Assertions.assertFalse(actualDate.isAfter(LocalDate.now()));
    }

    @Test
    void returns_now_datetime() {
        LocalDateTime actualDateTime = systemDateProvider.nowWithTime();
        Assertions.assertNotNull(actualDateTime);
        Assertions.assertFalse(actualDateTime.isAfter(LocalDateTime.now()));
    }
}
