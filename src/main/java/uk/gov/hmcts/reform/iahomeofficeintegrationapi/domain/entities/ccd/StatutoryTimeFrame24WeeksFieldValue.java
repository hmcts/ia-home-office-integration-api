package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.YesOrNo;

import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
@ToString
public class StatutoryTimeFrame24WeeksFieldValue {

    private YesOrNo status;
    private String reason;
    private String user;
    private String dateAdded;

    public StatutoryTimeFrame24WeeksFieldValue(
        YesOrNo status,
        String reason,
        String user,
        String dateAdded
    ) {
        this.status = requireNonNull(status);
        this.reason = requireNonNull(reason);
        this.user = requireNonNull(user);
        this.dateAdded = requireNonNull(dateAdded);
    }

    public YesOrNo getStatus() {
        return requireNonNull(status);
    }

    public String getReason() {
        return requireNonNull(reason);
    }

    public String getUser() {
        return requireNonNull(user);
    }

    public String getDateAdded() {
        return requireNonNull(dateAdded);
    }
}
