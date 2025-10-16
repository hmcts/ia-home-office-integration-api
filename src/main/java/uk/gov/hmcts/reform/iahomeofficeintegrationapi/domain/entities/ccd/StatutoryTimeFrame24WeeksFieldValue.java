package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.YesOrNo;

import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
@ToString
public class StatutoryTimeFrame24WeeksFieldValue {

    private YesOrNo stf24wStatus;
    private String stf24wStatusReason;
    private String user;
    private String dateAdded;

    public StatutoryTimeFrame24WeeksFieldValue(
        YesOrNo stf24wStatus,
        String stf24wStatusReason,
        String user,
        String dateAdded
    ) {
        this.stf24wStatus = requireNonNull(stf24wStatus);
        this.stf24wStatusReason = requireNonNull(stf24wStatusReason);
        this.user = requireNonNull(user);
        this.dateAdded = requireNonNull(dateAdded);
    }

    public YesOrNo getStf24wStatus() {
        return requireNonNull(stf24wStatus);
    }

    public String getStf24wStatusReason() {
        return requireNonNull(stf24wStatusReason);
    }

    public String getUser() {
        return requireNonNull(user);
    }

    public String getDateAdded() {
        return requireNonNull(dateAdded);
     }
}
