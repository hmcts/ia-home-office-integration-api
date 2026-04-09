package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPELLANT_DATE_OF_BIRTH;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPELLANT_NATIONALITIES;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.DIRECTIONS;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ApplicationStatus;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.CodeWithDescription;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ConsumerReference;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeCaseStatus;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeErrorResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.MessageHeader;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.Person;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.NationalityFieldValue;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.IdentityProvider;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.NotificationsHelper;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.config.HomeOfficeProperties;


@ExtendWith(MockitoExtension.class)
abstract class AbstractNotificationsHandlerTestBase {

    protected final String someCaseReference = "some-case";
    protected final String someHomeOfficeReference = "some-reference";
    protected final String identity = "someIdentity";
    protected final String firstName = "firstName";
    protected final String surname = "surname";
    protected final String someDocumentReference = "some-document-reference";
    protected final String directionExplanation = "direction explanation";
    protected final String dueDate = "2020-10-10";
    protected final String eventDateTime = "some-time";
    protected final String someCaseId = "someCaseId";

    @Mock
    protected Callback<AsylumCase> callback;
    @Mock
    protected CaseDetails<AsylumCase> caseDetails;
    @Mock
    protected AsylumCase asylumCase;
    @Mock
    protected Direction requestEvidenceDirection;
    @Mock
    protected HomeOfficeProperties homeOfficeProperties;
    @Mock
    protected HomeOfficeCaseStatus homeOfficeCaseStatus;
    @Mock
    protected ApplicationStatus applicationStatus;
    @Mock
    protected IdentityProvider identityProvider;
    @Mock
    protected NotificationsHelper notificationsHelper;
    @Mock
    protected ConsumerReference consumerReference;
    @Mock
    protected MessageHeader messageHeader;

    protected void setupCase(Event event) {

        when(callback.getEvent()).thenReturn(event);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    protected void setupHelperResponses() {
        when(notificationsHelper.getHomeOfficeReference(asylumCase)).thenReturn(someDocumentReference);
        when(notificationsHelper.getConsumerReference(someCaseReference)).thenReturn(consumerReference);
        when(notificationsHelper.getMessageHeader()).thenReturn(messageHeader);
        //setupHelperDirection(directionTag);
    }

    protected void setupHelperDirection(DirectionTag directionTag) {
        when(notificationsHelper.getDirectionContent(asylumCase, directionTag)).thenReturn(directionExplanation);
        when(notificationsHelper.getDirectionDeadline(asylumCase, directionTag)).thenReturn(dueDate);
    }

    protected void setupCaseData() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(someCaseReference));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(someHomeOfficeReference));
    }

    protected void setupDirection(DirectionTag directionTag) {
        when(requestEvidenceDirection.getTag()).thenReturn(directionTag);
        when(requestEvidenceDirection.getDateDue()).thenReturn(dueDate);
        when(requestEvidenceDirection.getExplanation()).thenReturn(directionExplanation);

        when(asylumCase.read(DIRECTIONS)).thenReturn(
            Optional.of(
                Collections.singletonList(
                    new IdValue<>("1", requestEvidenceDirection)
                )
            )
        );
    }

    protected void setupApplicantDetails() {

        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(firstName));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(surname));
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of("2000-01-01"));
    }

    protected void setupNationalities() {
        List<IdValue<NationalityFieldValue>> nationalitiesList = new ArrayList<>();
        Collections.addAll(nationalitiesList,
            new IdValue<>("0", new NationalityFieldValue("AU")),
            new IdValue<>("1", new NationalityFieldValue("BE")),
            new IdValue<>("2", new NationalityFieldValue("BR"))
        );

        when(asylumCase.read(APPELLANT_NATIONALITIES)).thenReturn(Optional.of(nationalitiesList));
    }

    protected void assertPerson(Person person) {

        assertThat(person).isNotNull();
        assertThat(person.getGivenName()).isEqualTo(firstName);
        assertThat(person.getFamilyName()).isEqualTo(surname);
        assertThat(person.getDayOfBirth()).isEqualTo(1);
        assertThat(person.getMonthOfBirth()).isEqualTo(1);
        assertThat(person.getYearOfBirth()).isEqualTo(2000);
        assertThat(person.getGender()).isNull();
    }

    protected void assertNationality(Person person) {
        assertThat(person.getNationality()).isNotNull();
        assertThat(person.getNationality().getCode()).isEqualTo("AU");
        assertThat(person.getNationality().getDescription()).isEqualTo("Australia");
    }

    protected void assertConsumerReference(ConsumerReference consumerReference) {

        assertThat(consumerReference).isNotNull();
        assertThat(consumerReference.getValue()).isEqualTo(someCaseReference);
        assertThat(consumerReference.getCode()).isEqualTo("HMCTS_CHALLENGE_REF");
        assertThat(consumerReference.getConsumer().getCode()).isEqualTo("HMCTS");
        assertThat(consumerReference.getConsumer().getDescription())
            .isEqualTo("HM Courts and Tribunal Service");
    }

    protected void assertMessageHeader(MessageHeader messageHeader) {

        assertThat(messageHeader).isNotNull();
        assertThat(messageHeader.getCorrelationId()).isEqualTo(identity);
        assertThat(messageHeader.getConsumer().getCode()).isEqualTo("HMCTS");
        assertThat(messageHeader.getConsumer().getDescription()).isEqualTo("HM Courts and Tribunal Service");
    }

    protected Map<String, HomeOfficeProperties.LookupReferenceData> getHomeOfficeReferenceData() {

        final Map<String, HomeOfficeProperties.LookupReferenceData> consumerMap = new HashMap<>();

        HomeOfficeProperties.LookupReferenceData lookupReferenceData = new HomeOfficeProperties.LookupReferenceData();
        lookupReferenceData.setCode("HMCTS_CHALLENGE_REF");
        lookupReferenceData.setDescription("HMCTS challenge reference");
        consumerMap.put("consumerReference", lookupReferenceData);

        lookupReferenceData = new HomeOfficeProperties.LookupReferenceData();
        lookupReferenceData.setCode("HMCTS");
        lookupReferenceData.setDescription("HM Courts and Tribunal Service");
        consumerMap.put("consumer", lookupReferenceData);

        return consumerMap;
    }

    protected HomeOfficeErrorResponse getResponse() {

        return new HomeOfficeErrorResponse(
            new MessageHeader(
                new CodeWithDescription("HMCTS", "HM Courts and Tribunal Service"),
                someCaseReference,
                eventDateTime),
            null);
    }
}
