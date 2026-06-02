package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain;

import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.YesOrNo;

public class CaseIncompatibleException extends RuntimeException {

    public CaseIncompatibleException(String message, YesOrNo stf24wStatus) {
        super(message + "  Intended 24-week status was " + stf24wStatus.toString().toUpperCase() + ".");
    }
}
