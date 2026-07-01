package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HomeOfficeChallengeTest {

    @Mock
    private Person person1;

    @Mock
    private Person person2;

    @Test
    void should_hold_onto_values() {
        List<Person> applicants = Arrays.asList(person1, person2);
        HomeOfficeChallenge challenge = new HomeOfficeChallenge(
            applicants,
            "appealType",
            "appealTierType",
            "2024-01-01"
        );

        assertNotNull(challenge);
        assertThat(challenge.getApplicants()).hasSize(2);
        assertEquals("appealType", challenge.getAppealType());
        assertEquals("appealTierType", challenge.getAppealTierType());
        assertEquals("2024-01-01", challenge.getChallengeSubmissionDate());
    }

    @Test
    void should_return_empty_list_when_applicants_is_null() {
        HomeOfficeChallenge challenge = new HomeOfficeChallenge(
            null,
            "appealType",
            "appealTierType",
            "2024-01-01"
        );

        assertNotNull(challenge.getApplicants());
        assertThat(challenge.getApplicants()).isEmpty();
    }

    @Test
    void should_return_unmodifiable_applicants_list() {
        List<Person> applicants = Arrays.asList(person1);
        HomeOfficeChallenge challenge = new HomeOfficeChallenge(
            applicants,
            "appealType",
            "appealTierType",
            "2024-01-01"
        );

        assertThatThrownBy(() -> challenge.getApplicants().add(person2))
            .isExactlyInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void should_handle_default_constructor() {
        HomeOfficeChallenge challenge = new HomeOfficeChallenge();

        assertNotNull(challenge.getApplicants());
        assertThat(challenge.getApplicants()).isEmpty();
    }

    @Test
    void should_allow_setting_values() {
        HomeOfficeChallenge challenge = new HomeOfficeChallenge();
        List<Person> applicants = Arrays.asList(person1);

        challenge.setApplicants(applicants);
        challenge.setAppealType("newAppealType");
        challenge.setAppealTierType("newAppealTierType");
        challenge.setChallengeSubmissionDate("2024-02-01");

        assertThat(challenge.getApplicants()).hasSize(1);
        assertEquals("newAppealType", challenge.getAppealType());
        assertEquals("newAppealTierType", challenge.getAppealTierType());
        assertEquals("2024-02-01", challenge.getChallengeSubmissionDate());
    }
}
