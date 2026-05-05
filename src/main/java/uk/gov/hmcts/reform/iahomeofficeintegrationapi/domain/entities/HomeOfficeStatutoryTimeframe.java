package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.IdValue;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(NON_NULL)
@EqualsAndHashCode(callSuper = true)
@Data
public class HomeOfficeStatutoryTimeframe extends HomeOfficeStatutoryTimeframeBase {
    // This is the class to use for the event data, as CCD needs all collections to be of type IdValue<...>
    @JsonProperty(value = "stf24weekCohorts", required = true)
    @NotNull
    @Valid
    private List<IdValue<Stf24WeekCohort>> stf24weekCohorts;

    // Create instance of this class from the raw DTO instance
    public HomeOfficeStatutoryTimeframe(HomeOfficeStatutoryTimeframeDto hoStatutoryTimeframeDto) {
        super(hoStatutoryTimeframeDto);
        // Set the cohorts field separately as it is different
        this.stf24weekCohorts = hoStatutoryTimeframeDto.getStf24weekCohorts().stream()
                                .map(cohort -> new IdValue<Stf24WeekCohort>(String.valueOf(cohort.getName().hashCode()), cohort)).toList();
    }
}
