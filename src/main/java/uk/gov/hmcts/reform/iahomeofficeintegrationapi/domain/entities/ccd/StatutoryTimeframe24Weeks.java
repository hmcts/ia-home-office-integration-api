package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeStatutoryTimeframe;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.IdValue;

import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
@ToString
public class StatutoryTimeframe24Weeks {

    private List<IdValue<StatutoryTimeframe24WeeksHistory>> history;
    private HomeOfficeStatutoryTimeframe homeOfficeResponse;

    private StatutoryTimeframe24Weeks() {
    }

    public StatutoryTimeframe24Weeks(
        List<IdValue<StatutoryTimeframe24WeeksHistory>> history,
        HomeOfficeStatutoryTimeframe homeOfficeResponse
    ) {
        this.history = requireNonNull(history);
        this.homeOfficeResponse = requireNonNull(homeOfficeResponse);
    }

    public List<IdValue<StatutoryTimeframe24WeeksHistory>> getHistory() {
        return history != null ? Collections.unmodifiableList(history) : Collections.emptyList();
    }

    public HomeOfficeStatutoryTimeframe getHomeOfficeResponse() {
        return requireNonNull(homeOfficeResponse);
    }

}
