package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client;

import com.google.common.collect.ImmutableMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.util.HomeOfficeApiUtil;

@Service
public class HomeOfficeCaseStatusClient {

    private final RestTemplate restTemplate;
    private final String homeOfficeSearchByParametersUrl;
    private HomeOfficeApiUtil homeOfficeApiUtil;

    public HomeOfficeCaseStatusClient(
        @Value("${homeoffice.endpoint}") String homeOfficeEndpoint,
        @Value("${homeoffice.case.search.uri}") String homeOfficeSearchByParametersUrl,
        RestTemplate restTemplate,
        HomeOfficeApiUtil homeOfficeApiUtil
    ) {
        this.restTemplate = restTemplate;
        this.homeOfficeSearchByParametersUrl = homeOfficeEndpoint + homeOfficeSearchByParametersUrl;
        this.homeOfficeApiUtil = homeOfficeApiUtil;
    }

    public String getCaseStatus(AsylumCase asylumCase) {

        try {

            return restTemplate
                .postForObject(
                    homeOfficeSearchByParametersUrl,
                    createRequest(asylumCase),
                    String.class
                );

        } catch (RestClientResponseException clientEx) {
            throw new HomeOfficeResponseException(
                clientEx.getResponseBodyAsString(),
                clientEx
            );

        }
    }

    HttpEntity<Map<String, Object>> createRequest(AsylumCase asylumCase) {
        Map<String, Object> body = new LinkedHashMap<>();

        body.put("messageHeader", homeOfficeApiUtil.createMessageHeader());
        body.put("searchParams", createSearchParams(asylumCase));

        return new HttpEntity<>(body, homeOfficeApiUtil.getHomeOfficeHeader());
    }

    Object[] createSearchParams(AsylumCase asylumCase) {
        Map<String, Object> searchCaseID = ImmutableMap.of("spType", "DOCUMENT_REFERENCE",
            "spValue", asylumCase.read(
                AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER).orElse(""));
        return new Object[] {searchCaseID};
    }

}
