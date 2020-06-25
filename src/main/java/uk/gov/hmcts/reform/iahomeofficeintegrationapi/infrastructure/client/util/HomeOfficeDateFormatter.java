package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.util;

import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.DateProvider;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.SystemDateProvider;

@Service
public class HomeOfficeDateFormatter {

    private HomeOfficeDateFormatter() {
    }

    public static String getCurrentDateTime() {
        DateProvider dateProvider = new SystemDateProvider();
        return dateProvider.nowWithTime().format(
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));
    }

}
