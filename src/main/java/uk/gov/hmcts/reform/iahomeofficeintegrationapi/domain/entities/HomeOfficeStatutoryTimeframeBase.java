package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(NON_NULL)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
public abstract class HomeOfficeStatutoryTimeframeBase {

    @JsonProperty(value = "hmctsReferenceNumber", required = true)
    @NotNull
    @Pattern(regexp = "^(RP|PA|EA|HU|DC|EU|AG)/[0-9]{5}/[0-9]{4}$",
             message = "Home Office reference ID must be of the form XX/12345/2026, where XX is the appeal type, " + 
                       "12345 stands for any five-digit number and 2026 is the year")
    private String hmctsReferenceNumber;

    @JsonProperty(value = "uan")
    @Pattern(regexp = "^[0-9]{4}-[0-9]{4}-[0-9]{4}-[0-9]{4}$", 
             message = "UAN must be in format XXXX-XXXX-XXXX-XXXX where X is a digit")
    private String uan;

    @JsonProperty(value = "familyName", required = true)
    @NotNull
    private String familyName;

    @JsonProperty(value = "givenNames", required = true)
    @NotNull
    private String givenNames;

    @JsonProperty(value = "dateOfBirth", required = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @NotNull
    private LocalDate dateOfBirth;

    @JsonProperty(value = "timeStamp", required = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @NotNull
    private OffsetDateTime timeStamp;

    // Copy constructor for base fields
    protected HomeOfficeStatutoryTimeframeBase(HomeOfficeStatutoryTimeframeBase other) {
        this.dateOfBirth = other.dateOfBirth;
        this.familyName = other.familyName;
        this.givenNames = other.givenNames;
        this.hmctsReferenceNumber = other.hmctsReferenceNumber;
        this.timeStamp = other.timeStamp;
        this.uan = other.uan;
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(NON_NULL)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @EqualsAndHashCode
    @Data
    public static class Stf24WeekCohort {
        @JsonProperty(value = "name", required = true)
        @NotNull
        private String name;

        @JsonProperty(value = "included", required = true)
        @NotNull
        private String included;

        public Stf24WeekCohort(String name, boolean included) {
            this.name = name;
            this.included = included ? "true" : "false";
        }
    }
}
