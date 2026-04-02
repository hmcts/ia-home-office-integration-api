package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.YesOrNo;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeStatutoryTimeframeDto;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.IdValue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StatutoryTimeframe24WeeksTest {

    @Test
    void shouldCreateStatutoryTimeframe24WeeksWithValidData() {
        // Given
        List<IdValue<StatutoryTimeframe24WeeksHistory>> history = createHistory("1", YesOrNo.YES);

        HomeOfficeStatutoryTimeframeDto homeOfficeDto = createHomeOfficeDto();        
        // When
        StatutoryTimeframe24Weeks stf = new StatutoryTimeframe24Weeks(
            history,
            homeOfficeDto
        );

        // Then
        assertNotNull(stf);
        assertEquals(1, stf.getHistory().size());
    }

    private HomeOfficeStatutoryTimeframeDto createHomeOfficeDto() {
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
                )
            )
            .timeStamp(OffsetDateTime.parse("2024-01-01T10:00:00Z"))
            .build();
        return homeOfficeDto;
    }

    @Test
    void shouldThrowNullPointerExceptionWhenHistoryIsNull() {
        // When & Then
        assertThrows(NullPointerException.class, () -> new StatutoryTimeframe24Weeks(null, createHomeOfficeDto()));
    }

    @Test
    void shouldThrowNullPointerExceptionWhenDtoIsNull() {
        // When & Then
        assertThrows(NullPointerException.class, () -> new StatutoryTimeframe24Weeks(createHistory("1", YesOrNo.YES), null));
    }

    private List<IdValue<StatutoryTimeframe24WeeksHistory>> createHistory(String id, YesOrNo status) {
        List<IdValue<StatutoryTimeframe24WeeksHistory>> history = new ArrayList<>();
        StatutoryTimeframe24WeeksHistory historyEntry = new StatutoryTimeframe24WeeksHistory(
            status,
            "Test reason",
            "Test user",
            "2024-01-01T12:00:00"
        );
        history.add(new IdValue<>(id, historyEntry));
        return history;
    }
}
