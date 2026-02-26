package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.YesOrNo;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;

@EqualsAndHashCode
@ToString
public class HomeOfficeAppellant {

    private String familyName;
    private String givenNames;
    private LocalDate dateOfBirth;
    private String nationality;
    private YesOrNo roa;
    private YesOrNo asylumSupport;
    private YesOrNo hoFeeWaiver;
    private String language;
    private YesOrNo interpreterNeeded;

    public HomeOfficeAppellant(
        String familyName,
        String givenNames,
        LocalDate dateOfBirth,
        String nationality,
        YesOrNo roa,
        YesOrNo asylumSupport,
        YesOrNo hoFeeWaiver,
        String language,
        YesOrNo interpreterNeeded
    ) {
        this.familyName = requireNonNull(familyName);
        this.givenNames = requireNonNull(givenNames);
        this.dateOfBirth = requireNonNull(dateOfBirth);
        this.nationality = requireNonNull(nationality);
        this.roa = roa;
        this.asylumSupport = asylumSupport;
        this.hoFeeWaiver = hoFeeWaiver;
        this.language = requireNonNull(language);
        this.interpreterNeeded = interpreterNeeded;
    }

}
