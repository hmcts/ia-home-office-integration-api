package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

class HomeOfficeStatutoryTimeframeDtoTest {

    private HomeOfficeStatutoryTimeframeDto dto;

    @Test
    void should_test_equals_contract() {
        EqualsVerifier.simple()
            .forClass(HomeOfficeStatutoryTimeframeDto.class)
            .verify();
    }

    @Test
    void should_hold_onto_single_cohort() {

        HomeOfficeStatutoryTimeframeDto.Stf24WeekCohortDto cohort =
            HomeOfficeStatutoryTimeframeDto.Stf24WeekCohortDto.builder()
                .name("HU")
                .included(true)
                .build();

        dto = HomeOfficeStatutoryTimeframeDto.builder()
            .stf24weekCohortDtos(List.of(cohort))
            .build();

        assertEquals(1, dto.getStf24weekCohortDtos().size());
        assertEquals("HU", dto.getStf24weekCohortDtos().get(0).getName());
        assertTrue(dto.getStf24weekCohortDtos().get(0).isIncluded());
    }

    @Test
    void should_hold_onto_multiple_cohorts() {

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
            .stf24weekCohortDtos(List.of(cohort1, cohort2))
            .build();

        assertEquals(2, dto.getStf24weekCohortDtos().size());
    }

    @Test
    void should_handle_empty_cohorts() {

        dto = HomeOfficeStatutoryTimeframeDto.builder()
            .stf24weekCohortDtos(List.of())
            .build();

        assertEquals(0, dto.getStf24weekCohortDtos().size());
    }
}