package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HomeOfficeApplicationDtoTest {

    @Test
    void shouldCreateDtoWithAllFieldsSet() {
        HomeOfficeAppellantDto appellant = new HomeOfficeAppellantDto();
        appellant.setFamilyName("Silver");
        appellant.setGivenNames("Long John");
        appellant.setDateOfBirth(LocalDate.of(1990, 1, 1));
        appellant.setNationality("British");

        HomeOfficeApplicationDto dto = new HomeOfficeApplicationDto();
        dto.setHoClaimDate(LocalDate.of(2025, 1, 1));
        dto.setHoDecisionDate(LocalDate.of(2025, 2, 1));
        dto.setHoDecisionLetterDate(LocalDate.of(2025, 2, 10));
        dto.setAppellants(List.of(appellant));

        assertThat(dto.getHoClaimDate()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(dto.getHoDecisionDate()).isEqualTo(LocalDate.of(2025, 2, 1));
        assertThat(dto.getHoDecisionLetterDate()).isEqualTo(LocalDate.of(2025, 2, 10));
        assertThat(dto.getAppellants()).hasSize(1);
        assertThat(dto.getAppellants().get(0).getFamilyName()).isEqualTo("Silver");
    }

    @Test
    void shouldAllowNullDatesAndEmptyAppellantsList() {
        HomeOfficeApplicationDto dto = new HomeOfficeApplicationDto();
        dto.setHoClaimDate(null);
        dto.setHoDecisionDate(null);
        dto.setHoDecisionLetterDate(null);
        dto.setAppellants(null);

        assertThat(dto.getHoClaimDate()).isNull();
        assertThat(dto.getHoDecisionDate()).isNull();
        assertThat(dto.getHoDecisionLetterDate()).isNull();
        assertThat(dto.getAppellants()).isNull();
    }

    @Test
    void equalsAndHashCodeShouldWork() {
        HomeOfficeApplicationDto first = new HomeOfficeApplicationDto();
        first.setHoClaimDate(LocalDate.of(2025, 1, 1));
        first.setHoDecisionDate(LocalDate.of(2025, 2, 1));
        first.setHoDecisionLetterDate(LocalDate.of(2025, 2, 10));

        HomeOfficeApplicationDto second = new HomeOfficeApplicationDto();
        second.setHoClaimDate(LocalDate.of(2025, 1, 1));
        second.setHoDecisionDate(LocalDate.of(2025, 2, 1));
        second.setHoDecisionLetterDate(LocalDate.of(2025, 2, 10));

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
    }

    @Test
    void toStringShouldContainAllFields() {
        HomeOfficeApplicationDto dto = new HomeOfficeApplicationDto();
        dto.setHoClaimDate(LocalDate.of(2025, 1, 1));
        dto.setHoDecisionDate(LocalDate.of(2025, 2, 1));
        dto.setHoDecisionLetterDate(LocalDate.of(2025, 2, 10));

        String str = dto.toString();

        assertThat(str).contains("2025-01-01");
        assertThat(str).contains("2025-02-01");
        assertThat(str).contains("2025-02-10");
    }
}
