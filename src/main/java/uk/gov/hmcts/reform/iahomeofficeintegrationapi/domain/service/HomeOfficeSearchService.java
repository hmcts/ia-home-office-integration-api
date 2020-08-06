package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service;

import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.config.HomeOfficeProperties.LookupReferenceData;

import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
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

@Service
@Slf4j
public class HomeOfficeSearchService {

    private final HomeOfficeProperties homeOfficeProperties;
    private final HomeOfficeSearchApi homeOfficeSearchApi;

    public HomeOfficeSearchService(
        HomeOfficeProperties homeOfficeProperties, HomeOfficeSearchApi homeOfficeSearchApi) {
        this.homeOfficeProperties = homeOfficeProperties;
        this.homeOfficeSearchApi = homeOfficeSearchApi;
    }

    public HomeOfficeSearchResponse getCaseStatus(String homeOfficeReferenceNumber) {

        HomeOfficeSearch request = makeRequestBody(homeOfficeReferenceNumber);
        HomeOfficeSearchResponse searchResponse = homeOfficeSearchApi.getStatus(request);
        log.info("HomeOffice-CaseSearch response: {}", searchResponse);

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
