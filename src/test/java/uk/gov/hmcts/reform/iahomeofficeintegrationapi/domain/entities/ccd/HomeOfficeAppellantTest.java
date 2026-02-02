package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.YesOrNo;

class HomeOfficeAppellantTest {

    @Test
    void shouldThrowExceptionWhenFamilyNameIsNull() {
        assertThrows(NullPointerException.class, () ->
            new HomeOfficeAppellant(
                null,
                "Long John",
                LocalDate.of(1990, 1, 1),
                "British",
                YesOrNo.YES,
                YesOrNo.NO,
                YesOrNo.NO,
                "English",
                YesOrNo.NO
            )
        );
    }

    @Test
    void shouldThrowExceptionWhenGivenNamesIsNull() {
        assertThrows(NullPointerException.class, () ->
            new HomeOfficeAppellant(
                "Silver",
                null,
                LocalDate.of(1990, 1, 1),
                "British",
                YesOrNo.YES,
                YesOrNo.NO,
                YesOrNo.NO,
                "English",
                YesOrNo.NO
            )
        );
    }

    @Test
    void shouldThrowExceptionWhenDateOfBirthIsNull() {
        assertThrows(NullPointerException.class, () ->
            new HomeOfficeAppellant(
                "Silver",
                "Long John",
                null,
                "British",
                YesOrNo.YES,
                YesOrNo.NO,
                YesOrNo.NO,
                "English",
                YesOrNo.NO
            )
        );
    }

    @Test
    void shouldThrowExceptionWhenNationalityIsNull() {
        assertThrows(NullPointerException.class, () ->
            new HomeOfficeAppellant(
                "Silver",
                "Long John",
                LocalDate.of(1990, 1, 1),
                null,
                YesOrNo.YES,
                YesOrNo.NO,
                YesOrNo.NO,
                "English",
                YesOrNo.NO
            )
        );
    }

    @Test
    void shouldThrowExceptionWhenLanguageIsNull() {
        assertThrows(NullPointerException.class, () ->
            new HomeOfficeAppellant(
                "Silver",
                "Long John",
                LocalDate.of(1990, 1, 1),
                "British",
                YesOrNo.YES,
                YesOrNo.NO,
                YesOrNo.NO,
                null,
                YesOrNo.NO
            )
        );
    }

    @Test
    void shouldAllowOptionalFieldsToBeNull() {
        HomeOfficeAppellant appellant =
            new HomeOfficeAppellant(
                "Silver",
                "Long John",
                LocalDate.of(1990, 1, 1),
                "British",
                null,
                null,
                null,
                "English",
                null
            );

        // no exception = test passes
    }

    @Test
    void shouldCreateAppellantWhenAllFieldsProvided() {
        HomeOfficeAppellant appellant =
            new HomeOfficeAppellant(
                "Smith",
                "John",
                LocalDate.of(1990, 1, 1),
                "British",
                YesOrNo.YES,
                YesOrNo.NO,
                YesOrNo.NO,
                "English",
                YesOrNo.YES
            );

        // no assertion needed; creation is the behaviour
    }

    @Test
    void shouldBeEqualWhenAllFieldsMatch() {
        HomeOfficeAppellant first =
            new HomeOfficeAppellant(
                "Smith",
                "John",
                LocalDate.of(1990, 1, 1),
                "British",
                YesOrNo.YES,
                YesOrNo.NO,
                YesOrNo.NO,
                "English",
                YesOrNo.YES
            );

        HomeOfficeAppellant second =
            new HomeOfficeAppellant(
                "Smith",
                "John",
                LocalDate.of(1990, 1, 1),
                "British",
                YesOrNo.YES,
                YesOrNo.NO,
                YesOrNo.NO,
                "English",
                YesOrNo.YES
            );

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
    }

    @Test
    void toStringShouldContainClassName() {
        HomeOfficeAppellant appellant =
            new HomeOfficeAppellant(
                "Smith",
                "John",
                LocalDate.of(1990, 1, 1),
                "British",
                null,
                null,
                null,
                "English",
                null
            );

        assertThat(appellant.toString())
            .contains("HomeOfficeAppellant")
            .contains("Smith");
    }

}
