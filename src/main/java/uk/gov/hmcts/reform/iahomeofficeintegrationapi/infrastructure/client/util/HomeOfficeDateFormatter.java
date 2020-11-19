package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.DateProvider;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.SystemDateProvider;

@Service
@Slf4j
public class HomeOfficeDateFormatter {

    private static final DateTimeFormatter HO_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter HO_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(
        "yyyy-MM-dd'T'HH:mm:ss'Z'");
    private static final DateTimeFormatter DD_MON_YEAR_FORMATTER = DateTimeFormatter.ofPattern("dd' 'MMM' 'yyyy");
    private static final DateTimeFormatter DD_MM_YYYY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private HomeOfficeDateFormatter() {
    }

    public static String getCurrentDateTime() {
        DateProvider dateProvider = new SystemDateProvider();
        return dateProvider.nowWithTime().format(HO_DATE_TIME_FORMATTER);
    }

    public static String getIacDateTime(String homeOfficeDate) {
        try {
            if (homeOfficeDate != null) {
                LocalDate parsedDate;
                //Date-time is provided
                if (homeOfficeDate.length() > 10) {
                    parsedDate = LocalDate.parse(homeOfficeDate, HO_DATE_TIME_FORMATTER);
                } else {
                    //only Date is provided
                    parsedDate = LocalDate.parse(homeOfficeDate, HO_DATE_FORMATTER);
                }
                return parsedDate.format(DD_MON_YEAR_FORMATTER);
            }

        } catch (Exception e) {
            //We return the date we received from Home Office
            log.info("HO date format error. HO date {}", homeOfficeDate);
        }

        return homeOfficeDate;
    }

    public static String getPersonDateOfBirth(int dayOfBirth, int monthOfBirth, int yearOfBirth) {
        String hoDateOfBirth = String.format("%02d", dayOfBirth)
            + "/" + String.format("%02d", monthOfBirth)
            + "/" + yearOfBirth;
        try {
            LocalDate parsedDate = LocalDate.parse(hoDateOfBirth, DD_MM_YYYY_FORMATTER);
            return parsedDate.format(DD_MON_YEAR_FORMATTER);

        } catch (Exception e) {
            //We return the date we received from Home Office
            log.info("HO Date of birth format error. HO date {}", hoDateOfBirth);
        }
        return hoDateOfBirth;
    }

    public static String getIacDateAndTime(String homeOfficeDate) {

        final String timeFactor = "T00:00:00Z";

        try {
            if (homeOfficeDate != null) {

                if (homeOfficeDate.length() < 11) {
                    homeOfficeDate = homeOfficeDate + timeFactor;
                }
                LocalDateTime parsedDate = LocalDateTime.parse(homeOfficeDate, HO_DATE_TIME_FORMATTER);

                return parsedDate.format(HO_DATE_TIME_FORMATTER);
            }

        } catch (Exception e) {
            log.info("HO date format error. HO date {}", homeOfficeDate);
        }
        return homeOfficeDate;
    }
}
