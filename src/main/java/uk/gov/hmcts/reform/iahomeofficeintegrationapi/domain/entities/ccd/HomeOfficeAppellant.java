package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.YesOrNo;

@Getter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
public class HomeOfficeAppellant {

    private String pp;
    @NonNull
    private String familyName;
    @NonNull
    private String givenNames;
    @NonNull
    private String dateOfBirth;
    @NonNull
    private String nationality;
    private YesOrNo roa;
    private YesOrNo asylumSupport;
    private YesOrNo hoFeeWaiver;
    @NonNull
    private String language;
    private YesOrNo interpreterNeeded;

}
