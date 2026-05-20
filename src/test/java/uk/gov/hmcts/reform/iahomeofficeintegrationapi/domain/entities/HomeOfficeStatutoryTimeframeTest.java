package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.IdValue;

class HomeOfficeStatutoryTimeframeTest {

    private HomeOfficeStatutoryTimeframeDto dto;

    @BeforeEach
    void setUp() {

        HomeOfficeStatutoryTimeframeDto.Stf24WeekCohortDto cohort1 =
            HomeOfficeStatutoryTimeframeDto.Stf24WeekCohortDto.builder()
                .name("HU")
                .included(true)
                .build();

        HomeOfficeStatutoryTimeframeDto.Stf24WeekCohortDto cohort2 =
            HomeOfficeStatutoryTimeframeDto.Stf24WeekCohortDto.builder()
                .name("PA")
                .included(false)
                .build();

        dto = HomeOfficeStatutoryTimeframeDto.builder()
            .hmctsReferenceNumber("PA/12345/2026")
            .uan("1234-5678-9012-3456")
            .familyName("Smith")
            .givenNames("John")
            .dateOfBirth(LocalDate.of(1990, 1, 1))
            .timeStamp(OffsetDateTime.of(2023, 12, 1, 14, 30, 0, 0, ZoneOffset.UTC))
            .stf24weekCohortDtos(List.of(cohort1, cohort2))
            .build();
    }

    @Test
    void should_map_base_fields_correctly_via_constructor() {

        HomeOfficeStatutoryTimeframe result =
            new HomeOfficeStatutoryTimeframe(dto);

        assertEquals(dto.getHmctsReferenceNumber(), result.getHmctsReferenceNumber());
        assertEquals(dto.getUan(), result.getUan());
        assertEquals(dto.getFamilyName(), result.getFamilyName());
        assertEquals(dto.getGivenNames(), result.getGivenNames());
        assertEquals(dto.getDateOfBirth(), result.getDateOfBirth());
        assertEquals(dto.getTimeStamp(), result.getTimeStamp());
    }

    @Test
    void should_convert_cohorts_to_idvalue_wrappers() {

        HomeOfficeStatutoryTimeframe result =
            new HomeOfficeStatutoryTimeframe(dto);

        assertNotNull(result.getStf24weekCohorts());
        assertEquals(2, result.getStf24weekCohorts().size());

        IdValue<HomeOfficeStatutoryTimeframe.Stf24WeekCohort> first =
            result.getStf24weekCohorts().get(0);

        assertNotNull(first.getId());
        assertNotNull(first.getValue());

        assertEquals("HU", first.getValue().getName());
        assertEquals("true", first.getValue().getIncluded());

        IdValue<HomeOfficeStatutoryTimeframe.Stf24WeekCohort> second =
            result.getStf24weekCohorts().get(1);

        assertEquals("PA", second.getValue().getName());
        assertEquals("false", second.getValue().getIncluded());
    }

    @Test
    void should_generate_consistent_id_from_name_hashcode() {

        HomeOfficeStatutoryTimeframe result =
            new HomeOfficeStatutoryTimeframe(dto);

        String expectedId = String.valueOf("HU".hashCode());

        assertEquals(expectedId, result.getStf24weekCohorts().get(0).getId());
    }

    @Test
    void should_handle_multiple_cohorts_order_preserved() {

        HomeOfficeStatutoryTimeframe result =
            new HomeOfficeStatutoryTimeframe(dto);

        assertEquals(2, result.getStf24weekCohorts().size());

        assertEquals("HU", result.getStf24weekCohorts().get(0).getValue().getName());
        assertEquals("PA", result.getStf24weekCohorts().get(1).getValue().getName());
    }

    @Test
    void should_set_included_as_string_true_or_false_only() {

        HomeOfficeStatutoryTimeframe result =
            new HomeOfficeStatutoryTimeframe(dto);

        for (IdValue<HomeOfficeStatutoryTimeframe.Stf24WeekCohort> item
                : result.getStf24weekCohorts()) {

            assertTrue(
                "true".equals(item.getValue().getIncluded())
                    || "false".equals(item.getValue().getIncluded())
            );
        }
    }
}