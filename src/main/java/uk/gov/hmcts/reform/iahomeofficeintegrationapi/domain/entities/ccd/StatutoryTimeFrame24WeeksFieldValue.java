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
    private String dateTimeAdded;

    /**
     * Creates a new StatutoryTimeFrame24WeeksFieldValue.
     *
     * @param status the status
     * @param reason the reason
     * @param user the user
     * @param dateTimeAdded the date and time added in ISO 8601 format (e.g., "2020-06-15T17:35:38Z")
     */
    public StatutoryTimeFrame24WeeksFieldValue(
        YesOrNo status,
        String reason,
        String user,
        String dateTimeAdded
    ) {
        this.status = requireNonNull(status);
        this.reason = requireNonNull(reason);
        this.user = requireNonNull(user);
        this.dateTimeAdded = requireNonNull(dateTimeAdded);
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

    public String getDateTimeAdded() {
        return requireNonNull(dateTimeAdded);
    }
}
