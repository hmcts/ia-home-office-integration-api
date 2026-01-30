package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class HomeOfficeApplication {

    private boolean asylumSupport;
    private LocalDate hoClaimDate;
    private LocalDate hoDecisionDate;
    private LocalDate hoDecisionLetterDate;
    private List<HomeOfficeAppellant> appellants;

}
