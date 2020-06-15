package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service;

import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.config.HomeOfficeProperties.LookupReferenceData;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ConsumerInstruct;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ConsumerReference;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ConsumerType;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.CourtOutcome;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.CourtType;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeInstruct;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.MessageHeader;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.MessageType;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.Outcome;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.HomeOfficeInstructApi;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.util.HomeOfficeApiUtil;
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

    public MessageHeader sendNotification(String homeOfficeReferenceNumber, String caseId) {

        HomeOfficeInstruct request = makeRequestBody(homeOfficeReferenceNumber, caseId);
        MessageHeader instructResponse = makeRequest(request);
        log.debug("HomeOffice-Instruct response: {}", instructResponse);

        return instructResponse;
    }

    public MessageHeader makeRequest(HomeOfficeInstruct request) {

        return homeOfficeInstructApi.sendNotification(request);
    }

    public HomeOfficeInstruct makeRequestBody(String homeOfficeReferenceNumber, String caseId) {

        LookupReferenceData consumerParent = homeOfficeProperties.getHomeOfficeReferenceData().get("consumerInstruct");
        LookupReferenceData consumer = homeOfficeProperties.getHomeOfficeReferenceData().get("consumer");
        final ConsumerType consumerType = new ConsumerType(consumer.getCode(), consumer.getDescription());

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
            HomeOfficeApiUtil.generateUuid(),
            HomeOfficeApiUtil.getCurrentDateTime());

        return new HomeOfficeInstruct(
            new ConsumerReference(consumerInstruct),
            courtOutcome,
            homeOfficeReferenceNumber,
            messageHeader,
            MessageType.REQUEST_CHALLENGE_END.toString()
        );

    }
}
