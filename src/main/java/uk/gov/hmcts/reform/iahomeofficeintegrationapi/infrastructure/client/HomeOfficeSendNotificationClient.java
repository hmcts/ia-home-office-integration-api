package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.util.HomeOfficeApiUtil;

@Service
public class HomeOfficeSendNotificationClient {

    private final RestTemplate restTemplate;
    private final String homeOfficeSendInstructUrl;
    private HomeOfficeApiUtil homeOfficeApiUtil;

    public HomeOfficeSendNotificationClient(
        @Value("${homeoffice.endpoint}") String homeOfficeEndpoint,
        @Value("${homeoffice.instruct.uri}") String homeOfficeSendInstructUrl,
        RestTemplate restTemplate,
        HomeOfficeApiUtil homeOfficeApiUtil
    ) {
        this.restTemplate = restTemplate;
        this.homeOfficeSendInstructUrl = homeOfficeEndpoint + homeOfficeSendInstructUrl;
        this.homeOfficeApiUtil = homeOfficeApiUtil;
    }

    public int sendNotification(AsylumCase asylumCase) {

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                homeOfficeSendInstructUrl,
                createRequest(asylumCase),
                String.class);

            return response.getStatusCode().value();

        } catch (RestClientResponseException clientEx) {
            throw new HomeOfficeResponseException(
                clientEx.getResponseBodyAsString(),
                clientEx
            );

        }
    }

    public HttpEntity<Map<String, Object>> createRequest(AsylumCase asylumCase) {
        Map<String, Object> body = new HashMap<>();
        body.put("consumerReference", homeOfficeApiUtil.createConsumerReference());
        body.put("courtOutcome", homeOfficeApiUtil.createCourtOutcome());
        body.put("hoReference", asylumCase.read(
            AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER)
            .orElse(""));
        body.put("messageHeader", homeOfficeApiUtil.createMessageHeader());
        body.put("messageType", "REQUEST_CHALLENGE_END");

        return new HttpEntity<>(body, homeOfficeApiUtil.getHomeOfficeHeader());
    }

}
