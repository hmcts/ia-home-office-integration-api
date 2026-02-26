package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class HomeOfficeAppellantDtoTest {

    @Test
    void shouldCreateDtoWithAllFieldsSet() {
        HomeOfficeAppellantDto dto = new HomeOfficeAppellantDto();
        dto.setFamilyName("Silver");
        dto.setGivenNames("Long John");
        dto.setDateOfBirth(LocalDate.of(1990, 1, 1));
        dto.setNationality("British");
        dto.setRoa(Boolean.TRUE);
        dto.setAsylumSupport(Boolean.FALSE);
        dto.setHoFeeWaiver(Boolean.TRUE);
        dto.setLanguage("English");
        dto.setInterpreterNeeded(null); // optional field can be null

        assertThat(dto.getFamilyName()).isEqualTo("Silver");
        assertThat(dto.getGivenNames()).isEqualTo("Long John");
        assertThat(dto.getDateOfBirth()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(dto.getNationality()).isEqualTo("British");
        assertThat(dto.getRoa()).isTrue();
        assertThat(dto.getAsylumSupport()).isFalse();
        assertThat(dto.getHoFeeWaiver()).isTrue();
        assertThat(dto.getLanguage()).isEqualTo("English");
        assertThat(dto.getInterpreterNeeded()).isNull();
    }

    @Test
    void shouldAllowOptionalBooleanFieldsToBeNull() {
        HomeOfficeAppellantDto dto = new HomeOfficeAppellantDto();
        dto.setRoa(null);
        dto.setAsylumSupport(null);
        dto.setHoFeeWaiver(null);
        dto.setInterpreterNeeded(null);

        assertThat(dto.getRoa()).isNull();
        assertThat(dto.getAsylumSupport()).isNull();
        assertThat(dto.getHoFeeWaiver()).isNull();
        assertThat(dto.getInterpreterNeeded()).isNull();
    }

    @Test
    void equalsAndHashCodeShouldWork() {
        HomeOfficeAppellantDto first = new HomeOfficeAppellantDto();
        first.setFamilyName("Silver");
        first.setGivenNames("Long John");
        first.setDateOfBirth(LocalDate.of(1990, 1, 1));
        first.setNationality("British");
        first.setRoa(Boolean.TRUE);
        first.setAsylumSupport(Boolean.FALSE);
        first.setHoFeeWaiver(Boolean.TRUE);
        first.setLanguage("English");
        first.setInterpreterNeeded(Boolean.FALSE);

        HomeOfficeAppellantDto second = new HomeOfficeAppellantDto();
        second.setFamilyName("Silver");
        second.setGivenNames("Long John");
        second.setDateOfBirth(LocalDate.of(1990, 1, 1));
        second.setNationality("British");
        second.setRoa(Boolean.TRUE);
        second.setAsylumSupport(Boolean.FALSE);
        second.setHoFeeWaiver(Boolean.TRUE);
        second.setLanguage("English");
        second.setInterpreterNeeded(Boolean.FALSE);

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
    }

    @Test
    void toStringShouldContainAllFields() {
        HomeOfficeAppellantDto dto = new HomeOfficeAppellantDto();
        dto.setFamilyName("Silver");
        dto.setGivenNames("Long John");
        dto.setDateOfBirth(LocalDate.of(1990, 1, 1));
        dto.setNationality("British");
        dto.setRoa(Boolean.TRUE);
        dto.setAsylumSupport(Boolean.FALSE);
        dto.setHoFeeWaiver(Boolean.TRUE);
        dto.setLanguage("English");
        dto.setInterpreterNeeded(Boolean.FALSE);

        String str = dto.toString();

        assertThat(str).contains("Silver");
        assertThat(str).contains("Long John");
        assertThat(str).contains("1990-01-01");
        assertThat(str).contains("British");
        assertThat(str).contains("true");
        assertThat(str).contains("false");
        assertThat(str).contains("English");
    }
}
