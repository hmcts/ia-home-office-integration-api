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

    public static String getIacDateTime(String homeOfficeDate) {
        try {
            if (homeOfficeDate != null) {
                //only Date is provided
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                //Date-time is provided
                if (homeOfficeDate.length() > 10) {
                    formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
                }
                LocalDate parsedDate = LocalDate.parse(homeOfficeDate, formatter);
                formatter = DateTimeFormatter.ofPattern("dd' 'MMM' 'yyyy");
                return parsedDate.format(formatter);
            }

        } catch (Exception e) {
            //We return the date we received from Home Office
            log.info("HO Decision date format error. HO date {}", homeOfficeDate);
        }

        return homeOfficeDate;
    }

    public static String getPersonDateOfBirth(int dayOfBirth, int monthOfBirth, int yearOfBirth) {
        String hoDateOfBirth = String.format("%02d", dayOfBirth)
            + "/" + String.format("%02d", monthOfBirth)
            + "/" + yearOfBirth;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate parsedDate = LocalDate.parse(hoDateOfBirth, formatter);
            formatter = DateTimeFormatter.ofPattern("dd' 'MMM' 'yyyy");
            return parsedDate.format(formatter);

        } catch (Exception e) {
            //We return the date we received from Home Office
            log.info("HO Date of birth format error. HO date {}", hoDateOfBirth);
        }
        return hoDateOfBirth;
    }

}
