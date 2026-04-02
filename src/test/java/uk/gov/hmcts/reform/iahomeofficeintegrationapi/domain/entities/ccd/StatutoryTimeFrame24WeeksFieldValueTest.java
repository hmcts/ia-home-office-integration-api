package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeStatutoryTimeframeDto;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.YesOrNo;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

class StatutoryTimeFrame24WeeksFieldValueTest {

    private StatutoryTimeframe24Weeks statutoryTimeFrame24WeeksFieldValue;

    @Test
    void has_correct_values() {
        List<IdValue<StatutoryTimeframe24WeeksHistory>> historyList = new ArrayList<>();
        historyList.add(new IdValue<>("1", new StatutoryTimeframe24WeeksHistory(
            YesOrNo.YES,
            "Test reason",
            "Test user",
            "2024-01-01T10:00:00Z"
        )));

        // Prepare a dummy HomeOffice DTO for the constructor
        HomeOfficeStatutoryTimeframeDto homeOfficeDto = HomeOfficeStatutoryTimeframeDto.builder()
            .hmctsReferenceNumber("PA/12345/2026")
            .uan("1234-5678-9012-3456")
            .familyName("Smith")
            .givenNames("John")
            .dateOfBirth(LocalDate.of(1990, 1, 1))
            .stf24WeekCohorts(List.of(
                HomeOfficeStatutoryTimeframeDto.Stf24WeekCohort.builder()
                    .name("HU")
                    .included("true")
                    .build()
            ))
            .timeStamp(OffsetDateTime.parse("2024-01-01T10:00:00Z"))
            .build();
       
        statutoryTimeFrame24WeeksFieldValue = new StatutoryTimeframe24Weeks(
            historyList,
            homeOfficeDto
        );
        
        assertNotNull(statutoryTimeFrame24WeeksFieldValue.getHistory());
        assertEquals(1, statutoryTimeFrame24WeeksFieldValue.getHistory().size());
        
        StatutoryTimeframe24WeeksHistory history = statutoryTimeFrame24WeeksFieldValue.getHistory().get(0).getValue();
        assertEquals("Test reason", history.getReason());
        assertEquals("Test user", history.getUser());
        assertEquals("2024-01-01T10:00:00Z", history.getDateTimeAdded());
        // Check that DTO is retained correctly
        assertNotNull(statutoryTimeFrame24WeeksFieldValue.getHomeOfficeResponse());
        assertEquals("PA/12345/2026", statutoryTimeFrame24WeeksFieldValue.getHomeOfficeResponse().getHmctsReferenceNumber());
        assertEquals("John", statutoryTimeFrame24WeeksFieldValue.getHomeOfficeResponse().getGivenNames());
        assertEquals(1, statutoryTimeFrame24WeeksFieldValue.getHomeOfficeResponse().getStf24WeekCohorts().size());
    }

    @Test
    void should_create_object_with_all_fields() {
        YesOrNo status = YesOrNo.YES;
        String reason = "Test reason";

        String user = "Test user";
        String dateAdded = "2023-01-01T10:00:00Z";

        List<IdValue<StatutoryTimeframe24WeeksHistory>> historyList = new ArrayList<>();
        historyList.add(new IdValue<>("1", new StatutoryTimeframe24WeeksHistory(
            status, reason, user, dateAdded
        )));

        HomeOfficeStatutoryTimeframeDto homeOfficeDto = HomeOfficeStatutoryTimeframeDto.builder()
            .hmctsReferenceNumber("PA/12345/2026")
            .uan("1234-5678-9012-3456")
            .familyName("Smith")
            .givenNames("John")
            .dateOfBirth(LocalDate.of(1990, 1, 1))
            .stf24WeekCohorts(List.of(
                HomeOfficeStatutoryTimeframeDto.Stf24WeekCohort.builder()
                    .name("HU")
                    .included("true")
                    .build()
            ))
            .timeStamp(OffsetDateTime.parse("2024-01-01T10:00:00Z"))
            .build();

        StatutoryTimeframe24Weeks fieldValue = new StatutoryTimeframe24Weeks(
            historyList,
            homeOfficeDto
        );

        assertNotNull(fieldValue.getHistory());
        assertEquals(1, fieldValue.getHistory().size());
        
        StatutoryTimeframe24WeeksHistory history = fieldValue.getHistory().get(0).getValue();
        assertEquals(status, history.getStatus());
        assertEquals(reason, history.getReason());
        assertEquals(user, history.getUser());
        assertEquals(dateAdded, history.getDateTimeAdded());

        HomeOfficeStatutoryTimeframeDto homeOfficeResponse = fieldValue.getHomeOfficeResponse();
        assertNotNull(homeOfficeResponse);
        assertEquals(homeOfficeResponse.getHmctsReferenceNumber(), "PA/12345/2026");
        assertEquals(homeOfficeResponse.getUan(), "1234-5678-9012-3456");
        assertEquals(homeOfficeResponse.getFamilyName(), "Smith");
        assertEquals(homeOfficeResponse.getGivenNames(), "John");
        assertEquals(homeOfficeResponse.getDateOfBirth(), LocalDate.of(1990, 1, 1));
        assertNotNull(homeOfficeResponse.getStf24WeekCohorts());
        assertEquals(1, homeOfficeResponse.getStf24WeekCohorts().size());
        var cohort = homeOfficeResponse.getStf24WeekCohorts().get(0);
        assertEquals(cohort.getName(), "HU");
        assertEquals(cohort.getIncluded(), "true");
        assertEquals(homeOfficeResponse.getHmctsReferenceNumber(), "PA/12345/2026");
        assertEquals(homeOfficeResponse.getTimeStamp(), OffsetDateTime.parse("2024-01-01T10:00:00Z"));

    }
}
