package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeCaseStatus;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.Person;

@Component
public class HomeOfficeDataMatchHelper {

    private static final DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("yyyy-M-d");

    public boolean isApplicantMatched(HomeOfficeCaseStatus status, Person appellant, String appellantDateOfBirth) {
        return isApplicantNameMatched(status, appellant) || isApplicantDobMatched(status, appellantDateOfBirth);
    }

    private boolean isApplicantDobMatched(HomeOfficeCaseStatus status, String appellantDateOfBirth) {

        Person person = status.getPerson();

        LocalDate applicantDob =
                LocalDate.parse(person.getYearOfBirth()
                        + "-" + person.getMonthOfBirth()
                        + "-" + person.getDayOfBirth(), dtFormatter);

        return applicantDob.equals(LocalDate.parse(appellantDateOfBirth, dtFormatter));
    }

    private boolean isApplicantNameMatched(HomeOfficeCaseStatus status, Person appellant) {

        Person person = status.getPerson();

        if (person.getGivenName() != null && person.getFamilyName() != null) {

            return person.getGivenName().equalsIgnoreCase(appellant.getGivenName())
                    || person.getFamilyName().equalsIgnoreCase(appellant.getFamilyName());
        }

        return false;
    }
}
