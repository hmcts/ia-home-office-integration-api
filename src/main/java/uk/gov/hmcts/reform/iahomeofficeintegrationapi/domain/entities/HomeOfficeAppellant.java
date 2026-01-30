package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import java.time.LocalDate;
import lombok.Data;

@Data
public class HomeOfficeAppellant {

    private String familyName;
    private String givenNames;
    private LocalDate dateOfBirth;
    private String nationality;
    private boolean roa;
    private boolean hoFeeWaiver;
    private String language;
    private boolean interpreterNeeded;

}
