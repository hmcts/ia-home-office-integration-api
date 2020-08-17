package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service;

import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.config.HomeOfficeProperties.LookupReferenceData;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.CodeWithDescription;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ConsumerReference;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.CourtOutcome;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.CourtType;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeInstruct;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeInstructResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.MessageHeader;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.MessageType;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.Outcome;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.HomeOfficeInstructApi;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.util.HomeOfficeDateFormatter;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.config.HomeOfficeProperties;

@Service
@Slf4j
public class HomeOfficeInstructService {

    private final HomeOfficeProperties homeOfficeProperties;
    private final HomeOfficeInstructApi homeOfficeInstructApi;
    private final ObjectMapper objectMapper;

    public HomeOfficeInstructService(
        HomeOfficeProperties homeOfficeProperties,
        HomeOfficeInstructApi homeOfficeInstructApi,
        ObjectMapper objectMapper) {
        this.homeOfficeProperties = homeOfficeProperties;
        this.homeOfficeInstructApi = homeOfficeInstructApi;
        this.objectMapper = objectMapper;
    }

    public HomeOfficeInstructResponse sendNotification(
        String homeOfficeReferenceNumber,
        String caseId,
        String correlationId) throws JsonProcessingException {

        HomeOfficeInstruct request = makeRequestBody(homeOfficeReferenceNumber, caseId, correlationId);
        ObjectWriter objectWriter = this.objectMapper.writer().withDefaultPrettyPrinter();
        log.info("HomeOffice-Instruct request: {}", objectWriter.writeValueAsString(request));
        HomeOfficeInstructResponse instructResponse = homeOfficeInstructApi.sendNotification(request);
        log.info("HomeOffice-Instruct response: {}", objectWriter.writeValueAsString(instructResponse));

        return instructResponse;
    }

    public HomeOfficeInstruct makeRequestBody(String homeOfficeReferenceNumber, String caseId, String correlationId) {

        LookupReferenceData consumerParent = homeOfficeProperties.getCodes().get("consumerReference");
        LookupReferenceData consumer = homeOfficeProperties.getCodes().get("consumer");
        final CodeWithDescription consumerType = new CodeWithDescription(consumer.getCode(), consumer.getDescription());

        ConsumerReference consumerReference = new ConsumerReference(
            consumerParent.getCode(),
            consumerType,
            consumerParent.getDescription(),
            caseId
        );
        CourtOutcome courtOutcome = new CourtOutcome(
            CourtType.FIRST_TIER.toString(),
            Outcome.ALLOWED.toString());
        MessageHeader messageHeader = new MessageHeader(
            consumerType,
            correlationId,
            HomeOfficeDateFormatter.getCurrentDateTime());

        return new HomeOfficeInstruct(
            consumerReference,
            courtOutcome,
            homeOfficeReferenceNumber,
            messageHeader,
            MessageType.REQUEST_CHALLENGE_END.toString()
        );

    }
}
