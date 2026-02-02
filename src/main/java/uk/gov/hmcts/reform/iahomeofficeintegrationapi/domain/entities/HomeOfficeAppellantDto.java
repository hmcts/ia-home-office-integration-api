package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import java.time.LocalDate;
import lombok.Data;

@Data
public class HomeOfficeAppellantDto {

    private String familyName;
    private String givenNames;
    private LocalDate dateOfBirth;
    private String nationality;
    private Boolean roa;
    private Boolean asylumSupport;
    private Boolean hoFeeWaiver;
    private String language;
    private Boolean interpreterNeeded;

}
