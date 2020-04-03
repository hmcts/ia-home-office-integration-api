package uk.gov.hmcts.reform.hois.domain.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Person {
    private int dayOfBirth;
    private String familyName;
    private String fullName;

    @JsonProperty("gender")
    private FieldType gender;

    private String givenName;
    private int monthOfBirth;

    @JsonProperty("nationality")
    private FieldType nationality;

    private int yearOfBirth;

}
