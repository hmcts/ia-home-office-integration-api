package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service;

import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.config.HomeOfficeProperties.LookupReferenceData;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.CodeWithDescription;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeSearch;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeSearchResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.MessageHeader;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.SearchParams;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.HomeOfficeSearchApi;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.util.HomeOfficeDateFormatter;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.util.HomeOfficeRequestUuidGenerator;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.config.HomeOfficeProperties;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security.AccessTokenProvider;


@Service
@Slf4j
public class HomeOfficeSearchService {

    private final HomeOfficeProperties homeOfficeProperties;
    private final HomeOfficeSearchApi homeOfficeSearchApi;
    private final AccessTokenProvider accessTokenProvider;
    private final ObjectMapper objectMapper;

    public HomeOfficeSearchService(
        HomeOfficeProperties homeOfficeProperties,
        HomeOfficeSearchApi homeOfficeSearchApi,
        @Qualifier("homeOffice") AccessTokenProvider accessTokenProvider,
        ObjectMapper objectMapper) {
        this.homeOfficeProperties = homeOfficeProperties;
        this.homeOfficeSearchApi = homeOfficeSearchApi;
        this.objectMapper = objectMapper;
        this.accessTokenProvider = accessTokenProvider;
    }

    public HomeOfficeSearchResponse getCaseStatus(String homeOfficeReferenceNumber) throws JsonProcessingException {

        final String accessToken = accessTokenProvider.getAccessToken();
        HomeOfficeSearch request = makeRequestBody(homeOfficeReferenceNumber);
        ObjectWriter objectWriter = this.objectMapper.writer().withDefaultPrettyPrinter();
        log.info("HomeOffice-CaseStatusSearch request: {}", objectWriter.writeValueAsString(request));
        HomeOfficeSearchResponse searchResponse = homeOfficeSearchApi.getStatus(accessToken, request);
        log.info("HomeOffice-CaseStatusSearch response: {}", objectWriter.writeValueAsString(searchResponse));

        return searchResponse;
    }


    public HomeOfficeSearch makeRequestBody(String homeOfficeReferenceNumber) {

        LookupReferenceData consumer = homeOfficeProperties.getCodes().get("consumer");
        final CodeWithDescription consumerType = new CodeWithDescription(consumer.getCode(), consumer.getDescription());
        return new HomeOfficeSearch(
            new MessageHeader(
                consumerType,
                HomeOfficeRequestUuidGenerator.generateUuid(),
                HomeOfficeDateFormatter.getCurrentDateTime()
            ),
            Collections.singletonList(new SearchParams("DOCUMENT_REFERENCE", homeOfficeReferenceNumber))
        );

    }
}
