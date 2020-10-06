package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeInstruct;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeInstructResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.HomeOfficeInstructApi;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security.AccessTokenProvider;


@Service
@Slf4j
public class HomeOfficeInstructService {

    private final HomeOfficeInstructApi homeOfficeInstructApi;
    private final AccessTokenProvider accessTokenProvider;
    private final ObjectMapper objectMapper;

    public HomeOfficeInstructService(
        HomeOfficeInstructApi homeOfficeInstructApi,
        @Qualifier("homeOffice") AccessTokenProvider accessTokenProvider,
        ObjectMapper objectMapper) {
        this.homeOfficeInstructApi = homeOfficeInstructApi;
        this.accessTokenProvider = accessTokenProvider;
        this.objectMapper = objectMapper;
    }

    public String sendNotification(
        HomeOfficeInstruct request
    ) {

        final String accessToken = accessTokenProvider.getAccessToken();
        ObjectWriter objectWriter = this.objectMapper.writer().withDefaultPrettyPrinter();

        HomeOfficeInstructResponse instructResponse;
        String status;
        try {
            log.info("HomeOffice-Instruct request: {}", objectWriter.writeValueAsString(request));
            instructResponse = homeOfficeInstructApi.sendNotification(accessToken, request);
            log.info("HomeOffice-Instruct response: {}", objectWriter.writeValueAsString(instructResponse));

            if (instructResponse == null || instructResponse.getMessageHeader() == null) {
                log.error("Error sending notification to Home Office for reference ID {}", request.getHoReference());
                status = "FAIL";
            } else {
                status = "OK";
            }
        } catch (JsonProcessingException e) {
            log.error("Json error sending notification to Home office for reference {}, Message: {}: "
                      + e.getMessage());
            status = "FAIL";

        } catch (Exception e) {
            log.error("Error sending notification to Home office for reference {}, Message: {}",
                request.getHoReference(), e.getMessage());
            status = "FAIL";
        }

        return status;
    }

    public MessageHeader getMessageHeader(String correlationId) {

        LookupReferenceData consumer = homeOfficeProperties.getCodes().get("consumer");
        final CodeWithDescription consumerType = new CodeWithDescription(consumer.getCode(), consumer.getDescription());

        return new MessageHeader(
            consumerType,
            correlationId,
            HomeOfficeDateFormatter.getCurrentDateTime());
    }

    public ConsumerReference getConsumerReference(String caseId) {

        LookupReferenceData consumerParent = homeOfficeProperties.getCodes().get("consumerReference");
        LookupReferenceData consumer = homeOfficeProperties.getCodes().get("consumer");
        final CodeWithDescription consumerType = new CodeWithDescription(consumer.getCode(), consumer.getDescription());

        return new ConsumerReference(
            consumerParent.getCode(),
            consumerType,
            consumerParent.getDescription(),
            caseId
        );
    }

    public Person getPerson(AsylumCase asylumCase) {

        final String givenName = asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse("");
        final String familyName = asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse("");
        final Person.PersonBuilder aPerson =
            person()
                .withGivenName(givenName)
                .withFamilyName(familyName)
                .withFullName(givenName + " " + familyName);

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

    public void buildCoreAttributes(
        RequestEvidenceBundleInstructMessageBuilder messageBuilder,
        MessageType messageType, String homeOfficeReferenceNumber, String caseId, String correlationId) {

        final ConsumerReference consumerReference = getConsumerReference(caseId);
        final MessageHeader messageHeader = getMessageHeader(correlationId);
        messageBuilder
            .withMessageHeader(messageHeader)
            .withHoReference(homeOfficeReferenceNumber)
            .withMessageType(messageType.toString())
            .withConsumerReference(consumerReference);

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

    public void extractDirectionAttributes(
        RequestEvidenceBundleInstructMessageBuilder messageBuilder,
        AsylumCase asylumCase, DirectionTag directionTag
    ) {

        Optional<List<IdValue<Direction>>> maybeExistingDirections = asylumCase.read(DIRECTIONS);

        final Optional<Direction> respondentEvidenceDirection = maybeExistingDirections
            .orElseThrow(() -> new IllegalStateException("directions not present"))
            .stream()
            .map(IdValue::getValue)
            .filter(direction -> directionTag.equals(direction.getTag()))
            .findFirst();

        String deadlineDate = null;
        String note = null;
        if (respondentEvidenceDirection.isPresent()) {
            deadlineDate = respondentEvidenceDirection.get().getDateDue();
            note = respondentEvidenceDirection.get().getExplanation();
        }
        messageBuilder.withDeadlineDate(deadlineDate);
        messageBuilder.withNote(note);
    }

}
