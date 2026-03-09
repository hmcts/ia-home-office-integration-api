package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
public class HomeOfficeStatutoryTimeframeDto {

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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "GMT")
    @NotNull
    private LocalDate dateOfBirth;

    @JsonProperty(value = "stf24weeks", required = true)
    @NotNull
    @Valid
    private Stf24Weeks stf24weeks;

    @JsonProperty(value = "timeStamp", required = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "GMT")
    @NotNull
    private LocalDateTime timeStamp;

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(NON_NULL)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @EqualsAndHashCode
    @Data
    public static class Stf24Weeks {
        @JsonProperty(value = "status", required = true)
        @NotNull
        @Pattern(regexp = "^[Yy][Ee][Ss]|[Nn][Oo]|[Nn][Uu][Ll][Ll]$", message = "Status must be 'Yes', 'No' or 'Null' (case-insensitive)")
        private String status;

        @JsonProperty(value = "cohorts", required = true)
        @NotNull
        private String[] cohorts;
    }
}
