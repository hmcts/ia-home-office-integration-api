package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.YesOrNo;

class HomeOfficeAppellantTest {

    @Test
    void shouldThrowExceptionWhenFamilyNameIsNull() {
        assertThrows(NullPointerException.class, () ->
            new HomeOfficeAppellant(
                "01",
                null,
                "Long John",
                "1990-01-01",
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
                null,
                "Silver",
                null,
                "1990-01-01",
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
                "02",
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
                null,
                "Silver",
                "Long John",
                "1990-01-01",
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
                "99",
                "Silver",
                "Long John",
                "1990-01-01",
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
                null,
                "Silver",
                "Long John",
                "1990-01-01",
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
                "01",
                "Smith",
                "John",
                "1990-01-01",
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
                "35",
                "Smith",
                "John",
                "1990-01-01",
                "British",
                YesOrNo.YES,
                YesOrNo.NO,
                YesOrNo.NO,
                "English",
                YesOrNo.YES
            );

        HomeOfficeAppellant second =
            new HomeOfficeAppellant(
                "35",
                "Smith",
                "John",
                "1990-01-01",
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
                null,
                "Smith",
                "John",
                "1990-01-01",
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
