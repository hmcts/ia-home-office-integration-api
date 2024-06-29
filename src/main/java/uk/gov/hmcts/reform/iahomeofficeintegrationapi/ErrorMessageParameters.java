package uk.gov.hmcts.reform.iahomeofficeintegrationapi;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ErrorMessageParameters {
    private String homeOfficeAppellantNotFoundErrorMessage;
    private String homeOfficeReferenceNotFoundErrorMessage;
    private String homeOfficeInvalidReferenceErrorMessage;
    private String homeOfficeCallErrorMessage;

}
