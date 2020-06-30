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

    public static String getIacDecisionDate(String hoDecisionDate) {
        return (hoDecisionDate != null && hoDecisionDate.length() == 20)
            ? hoDecisionDate.substring(8, 10)
            + "/" + hoDecisionDate.substring(5, 7)
            + "/" + hoDecisionDate.substring(0, 4)
            : "";
    }

}
