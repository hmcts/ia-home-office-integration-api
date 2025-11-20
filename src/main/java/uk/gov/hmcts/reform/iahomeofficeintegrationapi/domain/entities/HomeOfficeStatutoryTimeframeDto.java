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

    @JsonProperty(value = "ccdCaseNumber", required = true)
    @NotNull
    @Pattern(regexp = "^[0-9]{16}$", 
             message = "CCD Case Number must be a 16-digit number")
    private String ccdCaseNumber;

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

    @JsonProperty(value = "hoAcceleratedAppeal", required = true)
    private boolean hoStatutoryTimeframeStatus;

    @JsonProperty(value = "timeStamp", required = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "GMT")
    @NotNull
    private LocalDateTime timeStamp;

}
