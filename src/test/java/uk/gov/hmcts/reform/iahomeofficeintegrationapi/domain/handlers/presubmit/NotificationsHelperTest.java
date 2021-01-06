package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPEAL_SUBMISSION_DATE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPELLANT_NATIONALITIES;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.DIRECTIONS;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_CASE_STATUS_DATA;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ConsumerReference;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeCaseStatus;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeChallenge;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.MessageHeader;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.Person;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.NationalityFieldValue;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.NotificationsHelper;


@ExtendWith(MockitoExtension.class)
class NotificationsHelperTest extends AbstractNotificationsHandlerTestBase {

    NotificationsHelper notificationsHelper;

    @BeforeEach
    public void setUp() {

        notificationsHelper = new NotificationsHelper(homeOfficeProperties, identityProvider);
    }

    @Test
    void shouldReturnValidMessageHeader() {

        when(homeOfficeProperties.getCodes()).thenReturn(getHomeOfficeReferenceData());
        when(identityProvider.identity()).thenReturn(identity);

        final MessageHeader messageHeader = notificationsHelper.getMessageHeader();

        assertMessageHeader(messageHeader);
    }

    @Test
    void shouldReturnValidConsumerReference() {

        when(homeOfficeProperties.getCodes()).thenReturn(getHomeOfficeReferenceData());

        final ConsumerReference consumerReference =
            notificationsHelper.getConsumerReference(someCaseReference);

        assertConsumerReference(consumerReference);
    }

    @Test
    void shouldExtractPersonDetailsWithMultipleNationalitiesFromCase() {

        setupApplicantDetails();
        setupNationalities();

        final Person person = notificationsHelper.getPerson(asylumCase);

        assertPerson(person);
        assertNationality(person);
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    void shouldExtractPersonDetailsWithSingleNationalityFromCase() {

        setupApplicantDetails();
        List<IdValue<NationalityFieldValue>> nationalitiesList =
            Collections.singletonList(new IdValue<>("0", new NationalityFieldValue("AU")));
        when(asylumCase.read(APPELLANT_NATIONALITIES)).thenReturn(Optional.of(nationalitiesList));

        final Person person = notificationsHelper.getPerson(asylumCase);

        assertPerson(person);
        assertNationality(person);
    }

    @Test
    void shouldReturnHomeOfficeReferenceFromValidationResponseCase() {

        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(someHomeOfficeReference));
        when(asylumCase.read(HOME_OFFICE_CASE_STATUS_DATA, HomeOfficeCaseStatus.class))
            .thenReturn(Optional.of(homeOfficeCaseStatus));

        when(homeOfficeCaseStatus.getApplicationStatus()).thenReturn(applicationStatus);
        when(applicationStatus.getDocumentReference()).thenReturn(null);

        final String homeOfficeReference = notificationsHelper.getHomeOfficeReference(asylumCase);

        assertThat(homeOfficeReference).isEqualTo(someHomeOfficeReference).isNotEqualTo(someDocumentReference);
    }

    @Test
    void shouldReturnHomeOfficeReferenceFromCaseWhenDocumentReferenceNotFoundInValidationResponse() {

        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(someHomeOfficeReference));
        when(asylumCase.read(HOME_OFFICE_CASE_STATUS_DATA, HomeOfficeCaseStatus.class))
            .thenReturn(Optional.of(homeOfficeCaseStatus));

        when(homeOfficeCaseStatus.getApplicationStatus()).thenReturn(applicationStatus);
        when(applicationStatus.getDocumentReference()).thenReturn(someDocumentReference);

        final String homeOfficeReference = notificationsHelper.getHomeOfficeReference(asylumCase);

        assertThat(homeOfficeReference).isNotEqualTo(someHomeOfficeReference).isEqualTo(someDocumentReference);
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    void shouldExtractPersonDetailsWithStatelessFromCase() {

        setupApplicantDetails();
        List<IdValue<NationalityFieldValue>> nationalitiesList =
            Collections.singletonList(new IdValue<>("0", new NationalityFieldValue("ZZ")));

        when(asylumCase.read(APPELLANT_NATIONALITIES)).thenReturn(Optional.of(nationalitiesList));

        final Person person = notificationsHelper.getPerson(asylumCase);

        assertPerson(person);
        assertThat(person.getNationality()).isNotNull();
        assertThat(person.getNationality().getCode()).isEqualTo("ZZ");
        assertThat(person.getNationality().getDescription()).isEqualTo("Stateless");
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    void shouldBuildHomeOfficeChallengeFromCase() {

        final String appealType = "refusalOfHumanRights";
        final String submissionDate = "2020-09-22";
        when(asylumCase.read(APPEAL_TYPE, String.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(APPEAL_SUBMISSION_DATE, String.class)).thenReturn(Optional.of(submissionDate));

        setupApplicantDetails();
        setupNationalities();

        final HomeOfficeChallenge challenge = notificationsHelper.buildHomeOfficeChallenge(asylumCase);

        assertThat(challenge).isNotNull();
        assertThat(challenge.getAppealType()).isEqualTo(AppealType.from(appealType).get().name());
        assertThat(challenge.getChallengeSubmissionDate()).isEqualTo(submissionDate);

        final List<Person> applicants = challenge.getApplicants();
        assertThat(applicants).hasSize(1);
        assertPerson(applicants.get(0));
    }

    @Test
    void shouldExtractDirectionDueDateFromCase() {

        final String dueDate = "2020-10-10";

        when(requestEvidenceDirection.getTag()).thenReturn(DirectionTag.RESPONDENT_EVIDENCE);
        when(requestEvidenceDirection.getDateDue()).thenReturn(dueDate);

        when(asylumCase.read(DIRECTIONS)).thenReturn(
            Optional.of(
                Collections.singletonList(
                    new IdValue<>("1", requestEvidenceDirection)
                )
            )
        );

        final String directionDeadline =
            notificationsHelper.getDirectionDeadline(asylumCase, DirectionTag.RESPONDENT_EVIDENCE);

        assertThat(directionDeadline).isEqualTo(dueDate);
    }

    @Test
    void shouldExtractDirectionContentFromCase() {

        final String directionExplanation = "direction explanation";

        when(requestEvidenceDirection.getTag()).thenReturn(DirectionTag.RESPONDENT_EVIDENCE);
        when(requestEvidenceDirection.getExplanation()).thenReturn(directionExplanation);

        when(asylumCase.read(DIRECTIONS)).thenReturn(
            Optional.of(
                Collections.singletonList(
                    new IdValue<>("1", requestEvidenceDirection)
                )
            )
        );

        final String directionContent =
            notificationsHelper.getDirectionContent(asylumCase, DirectionTag.RESPONDENT_EVIDENCE);

        assertThat(directionContent).isEqualTo(directionExplanation);
    }
}
