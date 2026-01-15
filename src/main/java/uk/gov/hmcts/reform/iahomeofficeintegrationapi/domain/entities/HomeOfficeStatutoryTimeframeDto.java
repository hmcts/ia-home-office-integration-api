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
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;
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

    @JsonProperty(value = "ccdCaseId", required = true)
    @NotNull
    @Min(value = 1000000000000000L, message = "CCD Case ID must be a 16-digit number")
    @Max(value = 9999999999999999L, message = "CCD Case ID must be a 16-digit number")
    private Long ccdCaseId;

    @JsonProperty(value = "uan", required = true)
    @NotNull
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
        @Pattern(regexp = "^(([Yy][Ee][Ss])|([Nn][Oo]))$", message = "Status must be 'Yes', 'No', 'YES', 'NO', 'yes', or 'no'")
        private String status;

        @JsonProperty(value = "stf24wHomeOfficeCohort", required = true)
        @NotNull
        private String stf24wHomeOfficeCohort;
    }
}
