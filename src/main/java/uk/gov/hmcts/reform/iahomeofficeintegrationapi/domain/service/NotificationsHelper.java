package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service;

import static org.springframework.util.StringUtils.isEmpty;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPEAL_SUBMISSION_DATE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPELLANT_DATE_OF_BIRTH;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPELLANT_NATIONALITIES;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.DIRECTIONS;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.Person.PersonBuilder.person;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AppealTierType;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.CodeWithDescription;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ConsumerReference;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeCaseStatus;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeChallenge;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.MessageHeader;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.Person;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Nationality;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.NationalityFieldValue;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.util.HomeOfficeDateFormatter;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.config.HomeOfficeProperties;

@Component
@Slf4j
public class NotificationsHelper {

    private final HomeOfficeProperties homeOfficeProperties;
    private final IdentityProvider identityProvider;

    public NotificationsHelper(HomeOfficeProperties homeOfficeProperties, IdentityProvider identityProvider) {
        this.homeOfficeProperties = homeOfficeProperties;
        this.identityProvider = identityProvider;
    }

    public MessageHeader getMessageHeader() {

        HomeOfficeProperties.LookupReferenceData consumer =
            homeOfficeProperties.getCodes().get("consumer");
        final CodeWithDescription consumerType = new CodeWithDescription(consumer.getCode(), consumer.getDescription());

        return new MessageHeader(
            consumerType,
            identityProvider.identity(),
            HomeOfficeDateFormatter.getCurrentDateTime());
    }

    public ConsumerReference getConsumerReference(String caseId) {

        HomeOfficeProperties.LookupReferenceData consumerParent =
            homeOfficeProperties.getCodes().get("consumerReference");
        HomeOfficeProperties.LookupReferenceData consumer = homeOfficeProperties.getCodes().get("consumer");
        final CodeWithDescription consumerType = new CodeWithDescription(consumer.getCode(), consumer.getDescription());

        return new ConsumerReference(
            consumerParent.getCode(),
            consumerType,
            consumerParent.getDescription(),
            caseId
        );
    }

    public Person getPerson(AsylumCase asylumCase) {

        final Person.PersonBuilder aPerson =
            person()
                .withGivenName(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .withFamilyName(asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""));

        final Optional<String> mayBeDateOfBirth = asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class);
        if (mayBeDateOfBirth.isPresent()) {
            // extract day, month and year to set on Person
            final String dateOfBirth = mayBeDateOfBirth.get();
            final LocalDate parsedDateOfBirth = LocalDate.parse(dateOfBirth);
            aPerson
                .withDayOfBirth(parsedDateOfBirth.getDayOfMonth())
                .withMonthOfBirth(parsedDateOfBirth.getMonth().getValue())
                .withYearOfBirth(parsedDateOfBirth.getYear());
        }

        Optional<List<IdValue<NationalityFieldValue>>> nationalities = asylumCase.read(APPELLANT_NATIONALITIES);
        nationalities
            .flatMap(idValues -> idValues.stream()
                .map(IdValue::getValue)
                .findFirst())
            .ifPresent(nationalityFieldValue -> {
                final String code = nationalityFieldValue.getCode();
                aPerson.withNationality(new CodeWithDescription(code, Nationality.valueOf(code).toString()));
            });

        return aPerson.build();
    }

    public HomeOfficeChallenge buildHomeOfficeChallenge(AsylumCase asylumCase) {

        HomeOfficeChallenge challenge = new HomeOfficeChallenge();

        Optional<String> appealType = asylumCase.read(APPEAL_TYPE, String.class);
        appealType.ifPresent(s -> challenge.setAppealType(
            AppealType.from(s).get().name()
        ));

        challenge.setAppealTierType(AppealTierType.FIRST_TIER.toString());

        final String appealSubmissionDate = asylumCase.read(APPEAL_SUBMISSION_DATE, String.class).orElse("");
        challenge.setChallengeSubmissionDate(appealSubmissionDate);

        Person applicant = getPerson(asylumCase);
        challenge.setApplicants(Collections.singletonList(applicant));
        return challenge;
    }

    public String getDirectionContent(AsylumCase asylumCase, DirectionTag directionTag) {

        final Optional<Direction> respondentEvidenceDirection = getDirection(asylumCase, directionTag);

        String note = null;
        if (respondentEvidenceDirection.isPresent()) {
            note = respondentEvidenceDirection.get().getExplanation();
        }

        return note;
    }

    public String getDirectionDeadline(AsylumCase asylumCase, DirectionTag directionTag) {

        final Optional<Direction> respondentEvidenceDirection = getDirection(asylumCase, directionTag);

        String deadlineDate = null;
        if (respondentEvidenceDirection.isPresent()) {
            deadlineDate = respondentEvidenceDirection.get().getDateDue();
        }

        return deadlineDate;
    }

    public String getHomeOfficeReference(AsylumCase asylumCase) {

        String homeOfficeReferenceNumber = asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)
            .orElseThrow(() -> new IllegalStateException("Home office reference for the appeal is not present"));

        final Optional<HomeOfficeCaseStatus> homeOfficeCaseStatus =
            asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_CASE_STATUS_DATA, HomeOfficeCaseStatus.class);

        if (homeOfficeCaseStatus.isPresent()) {
            String documentReference = homeOfficeCaseStatus.get().getApplicationStatus().getDocumentReference();
            if (!isEmpty(documentReference)) {
                log.info("Using document reference {} from application status instead of home{}",
                    documentReference, homeOfficeReferenceNumber);
                homeOfficeReferenceNumber = documentReference;
            }
        }
        return homeOfficeReferenceNumber;
    }

    private Optional<Direction> getDirection(AsylumCase asylumCase, DirectionTag directionTag) {
        Optional<List<IdValue<Direction>>> maybeExistingDirections = asylumCase.read(DIRECTIONS);

        return maybeExistingDirections
            .orElseThrow(() -> new IllegalStateException("directions not present"))
            .stream()
            .map(IdValue::getValue)
            .filter(direction -> directionTag.equals(direction.getTag()))
            .findFirst();
    }
}
