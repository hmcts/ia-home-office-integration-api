package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service;

import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.config.HomeOfficeProperties.LookupReferenceData;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.CodeWithDescription;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ConsumerInstruct;
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

    public HomeOfficeInstructService(
        HomeOfficeProperties homeOfficeProperties, HomeOfficeInstructApi homeOfficeInstructApi) {
        this.homeOfficeProperties = homeOfficeProperties;
        this.homeOfficeInstructApi = homeOfficeInstructApi;
    }

    public HomeOfficeInstructResponse sendNotification(
        String homeOfficeReferenceNumber,
        String caseId,
        String correlationId) {

        HomeOfficeInstruct request = makeRequestBody(homeOfficeReferenceNumber, caseId, correlationId);
        HomeOfficeInstructResponse instructResponse = homeOfficeInstructApi.sendNotification(request);
        log.debug("HomeOffice-Instruct response: {}", instructResponse);

        return instructResponse;
    }

    public HomeOfficeInstruct makeRequestBody(String homeOfficeReferenceNumber, String caseId, String correlationId) {

        LookupReferenceData consumerParent = homeOfficeProperties.getCodes().get("consumerInstruct");
        LookupReferenceData consumer = homeOfficeProperties.getCodes().get("consumer");
        final CodeWithDescription consumerType = new CodeWithDescription(consumer.getCode(), consumer.getDescription());

        ConsumerInstruct consumerInstruct = new ConsumerInstruct(
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
            new ConsumerReference(consumerInstruct),
            courtOutcome,
            homeOfficeReferenceNumber,
            messageHeader,
            MessageType.REQUEST_CHALLENGE_END.toString()
        );

    }
}
