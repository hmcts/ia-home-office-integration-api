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

    @JsonProperty(value = "id", required = true)
    private String id;

    @JsonProperty(value = "ccdCaseNumber", required = true)
    private String ccdCaseNumber;

    @JsonProperty(value = "uan", required = true)
    private String uan;

    @JsonProperty(value = "familyName", required = true)
    private String familyName;

    @JsonProperty(value = "givenNames", required = true)
    private String givenNames;

    @JsonProperty(value = "dateOfBirth", required = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "GMT")
    private LocalDate dateOfBirth;

    @JsonProperty(value = "hoAcceleratedAppeal", required = true)
    private boolean hoStatutoryTimeframeStatus;

    @JsonProperty(value = "timeStamp", required = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "GMT")
    private LocalDateTime timeStamp;

}
