package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class HomeOfficeCaseStatusTest {

    @Mock
    Person person;
    @Mock
    ApplicationStatus applicationStatus;

    private HomeOfficeCaseStatus homeOfficeCaseStatus;

    @BeforeEach
    void setUp() {
        homeOfficeCaseStatus = new HomeOfficeCaseStatus(
            person, applicationStatus);
    }

    @Test
    void has_correct_values_after_setting() {

        homeOfficeCaseStatus = new HomeOfficeCaseStatus(person, applicationStatus);

        assertThat(homeOfficeCaseStatus).isNotNull();
        assertThat(homeOfficeCaseStatus.getPerson()).isNotNull();
        assertThat(homeOfficeCaseStatus.getApplicationStatus()).isNotNull();
        assertThat(homeOfficeCaseStatus.getPerson()).isEqualTo(person);
        assertThat(homeOfficeCaseStatus.getApplicationStatus()).isEqualTo(applicationStatus);
    }

    @Test
    void has_correct_values_after_setting_all_fields() {

        final String dob = "dob";
        final String someRejectionReason = "some rejection reason";
        final String decisionDate = "decision date";
        final String decisionSentDate = "decision sent date";
        final String datetimeMetadata = "datetime metadata";
        final String booleanMetadata = "boolean metadata";
        final String appellantDetailsTitle = "appellant details title";
        final String applicationDetailsTitle = "application details title";

        homeOfficeCaseStatus = new HomeOfficeCaseStatus(
            person, applicationStatus, dob, someRejectionReason, decisionDate, decisionSentDate,
            booleanMetadata, datetimeMetadata, appellantDetailsTitle, applicationDetailsTitle
        );

        assertThat(homeOfficeCaseStatus).isNotNull();
        assertThat(homeOfficeCaseStatus.getPerson()).isNotNull();
        assertThat(homeOfficeCaseStatus.getApplicationStatus()).isNotNull();
        assertThat(homeOfficeCaseStatus.getPerson()).isEqualTo(person);
        assertThat(homeOfficeCaseStatus.getApplicationStatus()).isEqualTo(applicationStatus);
        assertThat(homeOfficeCaseStatus.getDisplayDateOfBirth()).isEqualTo(dob);
        assertThat(homeOfficeCaseStatus.getDisplayRejectionReasons()).isEqualTo(someRejectionReason);
        assertThat(homeOfficeCaseStatus.getDisplayDecisionDate()).isEqualTo(decisionDate);
        assertThat(homeOfficeCaseStatus.getDisplayDecisionSentDate()).isEqualTo(decisionSentDate);
        assertThat(homeOfficeCaseStatus.getDisplayMetadataValueBoolean()).isEqualTo(booleanMetadata);
        assertThat(homeOfficeCaseStatus.getDisplayMetadataValueDateTime()).isEqualTo(datetimeMetadata);
        assertThat(homeOfficeCaseStatus.getDisplayAppellantDetailsTitle()).isEqualTo(appellantDetailsTitle);
        assertThat(homeOfficeCaseStatus.getDisplayApplicationDetailsTitle()).isEqualTo(applicationDetailsTitle);
    }
}
