package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPEAL_SUBMISSION_DATE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPELLANT_DATE_OF_BIRTH;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPELLANT_NATIONALITIES;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.DIRECTIONS;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.RequestEvidenceBundleInstructMessage.RequestEvidenceBundleInstructMessageBuilder.requestEvidenceBundleInstructMessage;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.config.HomeOfficeProperties.LookupReferenceData;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Qualifier;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.CodeWithDescription;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ConsumerReference;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeChallenge;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeInstruct;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeInstructResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.MessageHeader;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.MessageType;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.Person;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.RequestEvidenceBundleInstructMessage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.RequestEvidenceBundleInstructMessage.RequestEvidenceBundleInstructMessageBuilder;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.NationalityFieldValue;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.HomeOfficeInstructApi;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.config.HomeOfficeProperties;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security.AccessTokenProvider;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class HomeOfficeInstructServiceTest {

    private final String someHoReference = "some-ho-reference";
    private final String someCaseReference = "some-case";
    private final String someCorrelationId = "some-id";
    private final String firstName = "firstName";
    private final String surname = "surname";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private HomeOfficeProperties homeOfficeProperties;
    @Mock
    private HomeOfficeInstructApi homeOfficeInstructApi;
    @Mock
    private @Qualifier("requestUser")
    AccessTokenProvider accessTokenProvider;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private Direction requestEvidenceDirection;

    private HomeOfficeInstructService homeOfficeInstructService;

    @BeforeEach
    public void setUp() {

        homeOfficeInstructService = new HomeOfficeInstructService(
            homeOfficeProperties, homeOfficeInstructApi, accessTokenProvider, objectMapper);
    }

    @Test
    void shouldReturnValidMessageHeader() {

        when(homeOfficeProperties.getCodes()).thenReturn(getHomeOfficeReferenceData());

        final MessageHeader messageHeader = homeOfficeInstructService.getMessageHeader(someCorrelationId);

        assertMessageHeader(messageHeader);
    }

    @Test
    void shouldReturnValidConsumerReference() {

        when(homeOfficeProperties.getCodes()).thenReturn(getHomeOfficeReferenceData());

        final ConsumerReference consumerReference =
            homeOfficeInstructService.getConsumerReference(someCaseReference);

        assertConsumerReference(consumerReference);
    }

    @Test
    void shouldExtractPersonDetailsWithMultipleNationalitiesFromCase() {

        setupApplicantDetails();
        setupNationalities();

        final Person person = homeOfficeInstructService.getPerson(asylumCase);

        assertPerson(person);
        assertNationality(person);
    }

    @Test
    void shouldExtractPersonDetailsWithSingleNationalityFromCase() {

        setupApplicantDetails();
        List<IdValue<NationalityFieldValue>> nationalitiesList =
            Collections.singletonList(new IdValue<>("0", new NationalityFieldValue("AU")));
        when(asylumCase.read(APPELLANT_NATIONALITIES)).thenReturn(Optional.of(nationalitiesList));

        final Person person = homeOfficeInstructService.getPerson(asylumCase);

        assertPerson(person);
        assertNationality(person);
    }

    @Test
    void shouldExtractPersonDetailsWithStatelessFromCase() {

        setupApplicantDetails();
        List<IdValue<NationalityFieldValue>> nationalitiesList =
            Collections.singletonList(new IdValue<>("0", new NationalityFieldValue("ZZ")));
        when(asylumCase.read(APPELLANT_NATIONALITIES)).thenReturn(Optional.of(nationalitiesList));

        final Person person = homeOfficeInstructService.getPerson(asylumCase);

        assertPerson(person);
        assertThat(person.getNationality()).isNotNull();
        assertThat(person.getNationality().getCode()).isEqualTo("ZZ");
        assertThat(person.getNationality().getDescription()).isEqualTo("Stateless");

    }

    private void setupNationalities() {
        List<IdValue<NationalityFieldValue>> nationalitiesList = new ArrayList<>();
        Collections.addAll(nationalitiesList,
            new IdValue<>("0", new NationalityFieldValue("AU")),
            new IdValue<>("1", new NationalityFieldValue("BE")),
            new IdValue<>("2", new NationalityFieldValue("BR"))
        );

        when(asylumCase.read(APPELLANT_NATIONALITIES)).thenReturn(Optional.of(nationalitiesList));
    }

    @Test
    void shouldBuildHomeOfficeChallengeFromCase() {

        final String appealType = "refusalOfHumanRights";
        final String submissionDate = "2020-09-22";
        when(asylumCase.read(APPEAL_TYPE, String.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(APPEAL_SUBMISSION_DATE, String.class)).thenReturn(Optional.of(submissionDate));

        setupApplicantDetails();
        setupNationalities();

        final HomeOfficeChallenge challenge = homeOfficeInstructService.buildHomeOfficeChallenge(asylumCase);

        assertThat(challenge).isNotNull();
        assertThat(challenge.getAppealType()).isEqualTo(AppealType.from(appealType).get().name());
        assertThat(challenge.getChallengeSubmissionDate()).isEqualTo(submissionDate);

        final List<Person> applicants = challenge.getApplicants();
        assertThat(applicants).hasSize(1);
        assertPerson(applicants.get(0));
    }

    @Test
    void shouldExtractDirectionAttributesFromCase() {

        final String dueDate = "2020-10-10";
        final String directionExplanation = "direction explanation";

        when(requestEvidenceDirection.getTag()).thenReturn(DirectionTag.RESPONDENT_EVIDENCE);
        when(requestEvidenceDirection.getDateDue()).thenReturn(dueDate);
        when(requestEvidenceDirection.getExplanation()).thenReturn(directionExplanation);

        when(asylumCase.read(DIRECTIONS)).thenReturn(
            Optional.of(
                Collections.singletonList(
                    new IdValue<>("1", requestEvidenceDirection)
                )
            )
        );

        RequestEvidenceBundleInstructMessageBuilder messageBuilder = requestEvidenceBundleInstructMessage();

        homeOfficeInstructService.extractDirectionAttributes(
            messageBuilder, asylumCase, DirectionTag.RESPONDENT_EVIDENCE);

        final RequestEvidenceBundleInstructMessage instructMessage = messageBuilder.build();
        assertThat(instructMessage).isNotNull();
        assertThat(instructMessage.getDeadlineDate()).isEqualTo(dueDate);
        assertThat(instructMessage.getNote()).isEqualTo(directionExplanation);
    }

    @Test
    void shouldBuildCoreAttributesFromCase() {

        RequestEvidenceBundleInstructMessageBuilder messageBuilder = requestEvidenceBundleInstructMessage();
        final MessageType messageType = MessageType.REQUEST_EVIDENCE_BUNDLE;

        when(homeOfficeProperties.getCodes()).thenReturn(getHomeOfficeReferenceData());

        homeOfficeInstructService.buildCoreAttributes(
            messageBuilder, messageType, someHoReference, someCaseReference, someCorrelationId);

        final RequestEvidenceBundleInstructMessage instructMessage = messageBuilder.build();
        assertThat(instructMessage.getHoReference()).isEqualTo(someHoReference);
        assertThat(instructMessage.getMessageType()).isEqualTo(messageType.toString());
        assertConsumerReference(instructMessage.getConsumerReference());
        assertMessageHeader(instructMessage.getMessageHeader());
    }

    @Test
    void shouldSetInstructStatusOkForValidRequest() {

        AsylumCase asylumCase = new AsylumCase();
        when(accessTokenProvider.getAccessToken()).thenReturn("some-access-token");
        when(homeOfficeInstructApi.sendNotification(anyString(), any(HomeOfficeInstruct.class)))
            .thenReturn(getResponse());

        final String response =
            homeOfficeInstructService.sendNotification(buildRequestMessage());

        assertThat(response).isEqualTo("OK");
    }

    @Test
    void shouldSetInstructStatusErrorForNullResponse() {

        AsylumCase asylumCase = new AsylumCase();
        when(accessTokenProvider.getAccessToken()).thenReturn("some-access-token");
        when(homeOfficeInstructApi.sendNotification(anyString(), any(HomeOfficeInstruct.class))).thenReturn(null);

        final String response =
            homeOfficeInstructService.sendNotification(buildRequestMessage());

        assertThat(response).isEqualTo("FAIL");
    }

    private void assertMessageHeader(MessageHeader messageHeader) {

        assertThat(messageHeader).isNotNull();
        assertThat(messageHeader.getCorrelationId()).isEqualTo(someCorrelationId);
        assertThat(messageHeader.getConsumer().getCode()).isEqualTo("HMCTS");
        assertThat(messageHeader.getConsumer().getDescription()).isEqualTo("HM Courts and Tribunal Service");
    }

    private void assertConsumerReference(ConsumerReference consumerReference) {

        assertThat(consumerReference).isNotNull();
        assertThat(consumerReference.getValue()).isEqualTo(someCaseReference);
        assertThat(consumerReference.getCode()).isEqualTo("HMCTS_CHALLENGE_REF");
        assertThat(consumerReference.getConsumer().getCode()).isEqualTo("HMCTS");
        assertThat(consumerReference.getConsumer().getDescription())
            .isEqualTo("HM Courts and Tribunal Service");
    }

    private void assertPerson(Person person) {

        assertThat(person).isNotNull();
        assertThat(person.getGivenName()).isEqualTo(firstName);
        assertThat(person.getFamilyName()).isEqualTo(surname);
        assertThat(person.getDayOfBirth()).isEqualTo(1);
        assertThat(person.getMonthOfBirth()).isEqualTo(1);
        assertThat(person.getYearOfBirth()).isEqualTo(2000);
        assertThat(person.getGender()).isNull();
    }

    private void assertNationality(Person person) {
        assertThat(person.getNationality()).isNotNull();
        assertThat(person.getNationality().getCode()).isEqualTo("AU");
        assertThat(person.getNationality().getDescription()).isEqualTo("Australia");
    }

    private void setupApplicantDetails() {

        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(firstName));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(surname));
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of("2000-01-01"));
    }

    private HomeOfficeInstruct buildRequestMessage() {

        return
            new RequestEvidenceBundleInstructMessage(
                null, someHoReference, null, "someMessageType", "01-01-2021", null, "direction content"
            );
    }

    private Map<String, LookupReferenceData> getHomeOfficeReferenceData() {

        final Map<String, LookupReferenceData> consumerMap = new HashMap<>();

        LookupReferenceData lookupReferenceData = new LookupReferenceData();
        lookupReferenceData.setCode("HMCTS_CHALLENGE_REF");
        lookupReferenceData.setDescription("HMCTS challenge reference");
        consumerMap.put("consumerReference", lookupReferenceData);

        lookupReferenceData = new LookupReferenceData();
        lookupReferenceData.setCode("HMCTS");
        lookupReferenceData.setDescription("HM Courts and Tribunal Service");
        consumerMap.put("consumer", lookupReferenceData);

        return consumerMap;
    }

    private HomeOfficeInstructResponse getResponse() {

        return new HomeOfficeInstructResponse(
            new MessageHeader(
                new CodeWithDescription("HMCTS", "HM Courts and Tribunal Service"),
                someCorrelationId,
                "some-time"),
            null);
    }
}
