package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

public class RejectionReason {
    private String reason;

    private RejectionReason() {

    }

    public RejectionReason(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}
