package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import static java.util.Objects.requireNonNull;

public class Person {

    private CodeWithDescription gender;
    private CodeWithDescription nationality;
    private String givenName;
    private String familyName;
    private String fullName;

    private Person() {

    }

    public Person(CodeWithDescription gender, CodeWithDescription nationality,
                  String givenName, String familyName, String fullName) {
        this.gender = gender;
        this.nationality = nationality;
        this.givenName = givenName;
        this.familyName = familyName;
        this.fullName = fullName;
    }

    public CodeWithDescription getGender() {
        return gender;
    }

    public CodeWithDescription getNationality() {
        return nationality;
    }

    public String getGivenName() {
        requireNonNull(givenName);
        return givenName;
    }

    public String getFamilyName() {
        requireNonNull(familyName);
        return familyName;
    }

    public String getFullName() {
        return fullName;
    }
}