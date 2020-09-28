package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

public class Person {

    private CodeWithDescription gender;
    private CodeWithDescription nationality;
    private String givenName;
    private String familyName;
    private String fullName;
    private int dayOfBirth;
    private int monthOfBirth;
    private int yearOfBirth;

    private Person() {

    }

    public Person(CodeWithDescription gender, CodeWithDescription nationality,
                  String givenName, String familyName, String fullName,
                  int dayOfBirth, int monthOfBirth, int yearOfBirth) {
        this.gender = gender;
        this.nationality = nationality;
        this.givenName = givenName;
        this.familyName = familyName;
        this.fullName = fullName;
        this.dayOfBirth = dayOfBirth;
        this.monthOfBirth = monthOfBirth;
        this.yearOfBirth = yearOfBirth;
    }

    public CodeWithDescription getGender() {
        return gender;
    }

    public CodeWithDescription getNationality() {
        return nationality;
    }

    public String getGivenName() {
        return givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public String getFullName() {
        return fullName;
    }

    public int getDayOfBirth() {
        return dayOfBirth;
    }

    public int getMonthOfBirth() {
        return monthOfBirth;
    }

    public int getYearOfBirth() {
        return yearOfBirth;
    }


    public static final class PersonBuilder {
        private CodeWithDescription gender;
        private CodeWithDescription nationality;
        private String givenName;
        private String familyName;
        private String fullName;
        private int dayOfBirth;
        private int monthOfBirth;
        private int yearOfBirth;

        private PersonBuilder() {
        }

        public static PersonBuilder person() {
            return new PersonBuilder();
        }

        public PersonBuilder withGender(CodeWithDescription gender) {
            this.gender = gender;
            return this;
        }

        public PersonBuilder withNationality(CodeWithDescription nationality) {
            this.nationality = nationality;
            return this;
        }

        public PersonBuilder withGivenName(String givenName) {
            this.givenName = givenName;
            return this;
        }

        public PersonBuilder withFamilyName(String familyName) {
            this.familyName = familyName;
            return this;
        }

        public PersonBuilder withFullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public PersonBuilder withDayOfBirth(int dayOfBirth) {
            this.dayOfBirth = dayOfBirth;
            return this;
        }

        public PersonBuilder withMonthOfBirth(int monthOfBirth) {
            this.monthOfBirth = monthOfBirth;
            return this;
        }

        public PersonBuilder withYearOfBirth(int yearOfBirth) {
            this.yearOfBirth = yearOfBirth;
            return this;
        }

        public Person build() {
            return
                new Person(gender, nationality, givenName, familyName, fullName, dayOfBirth, monthOfBirth, yearOfBirth);
        }
    }
}
