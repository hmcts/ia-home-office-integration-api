package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security.homeoffice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security.idam.IdentityManagerResponseException;

@Service
@Slf4j
public class HomeOfficeAuthorizor {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String tokenPath;
    private final String clientId;
    private final String clientSecret;

    public HomeOfficeAuthorizor(
        RestTemplate restTemplate,
        @Value("${auth.homeoffice.client.baseUrl}") String baseUrl,
        @Value("${auth.homeoffice.token.path}") String tokenPath,
        @Value("${auth.homeoffice.client.id}") String clientId,
        @Value("${auth.homeoffice.client.secret}") String clientSecret
    ) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.tokenPath = tokenPath;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public String fetchCodeAuthorization() {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        log.info("Requesting JWT token from Home Office: {}", baseUrl + tokenPath);

        String response;
        try {
            response =
                restTemplate
                    .exchange(
                        baseUrl + tokenPath,
                        HttpMethod.POST,
                        requestEntity,
                        new ParameterizedTypeReference<String>() {}
                    ).getBody();
            log.info("Received JWT token response from Home Office");

        } catch (RestClientResponseException e) {

            log.error("Error retrieving token from Home Office: " + e.getMessage());
            throw new IdentityManagerResponseException(
                "Could not get auth code with Home Office",
                e
            );
        }
        return "Bearer " + extractAccessToken(response);
    }

    private String extractAccessToken(String response) {

        JacksonJsonParser jsonParser = new JacksonJsonParser();
        final String accessToken = jsonParser.parseMap(response).get("access_token").toString();
        log.info("Extracted access token from the response");
        return accessToken;
    }
}
