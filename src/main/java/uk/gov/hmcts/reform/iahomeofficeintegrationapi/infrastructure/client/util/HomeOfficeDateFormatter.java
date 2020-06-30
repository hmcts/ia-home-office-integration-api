package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.DateProvider;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.SystemDateProvider;

@Service
@Slf4j
public class HomeOfficeDateFormatter {

    private HomeOfficeDateFormatter() {
    }

    public static String getCurrentDateTime() {
        DateProvider dateProvider = new SystemDateProvider();
        return dateProvider.nowWithTime().format(
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));
    }

    public static String getIacDecisionDate(String hoDecisionDate) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
            LocalDate parsedDate = LocalDate.parse(hoDecisionDate, formatter);
            formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return parsedDate.format(formatter);
        } catch (Exception e) {
            //We return the date we received from Home Office
            log.info("HO Decision date format error. HO date {}", hoDecisionDate);
        }

        return hoDecisionDate;
    }

}
