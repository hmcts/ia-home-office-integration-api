package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security.homeoffice;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.HomeOfficeTokenApi;


@Service
@Slf4j
public class HomeOfficeAuthorizor {

    private final HomeOfficeTokenApi homeOfficeTokenApi;
    private final String baseUrl;
    private final String tokenPath;
    private final String clientId;
    private final String clientSecret;

    public HomeOfficeAuthorizor(
        HomeOfficeTokenApi homeOfficeTokenApi,
        @Value("${auth.homeoffice.client.baseUrl}") String baseUrl,
        @Value("${auth.homeoffice.token.path}") String tokenPath,
        @Value("${auth.homeoffice.client.id}") String clientId,
        @Value("${auth.homeoffice.client.secret}") String clientSecret
    ) {
        this.homeOfficeTokenApi = homeOfficeTokenApi;
        this.baseUrl = baseUrl;
        this.tokenPath = tokenPath;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public String fetchCodeAuthorization() {
        warnAboutMissingSecrets();

        Map<String, String> body = new HashMap<>();
        body.put("client_id", clientId);
        body.put("client_secret", clientSecret);
        body.put("grant_type", "client_credentials");

        log.info("Requesting JWT token from Home Office: {}", baseUrl + tokenPath);

        String response = homeOfficeTokenApi.getAuthorizationToken(body);
        if (StringUtils.isBlank(response)) {
            log.warn("The response from the Home Office Auth Token Api is null or empty. You may have a problem.");
        }

        return "Bearer " + extractAccessToken(response);
    }

    private String extractAccessToken(String response) {

        JacksonJsonParser jsonParser = new JacksonJsonParser();
        final String accessToken = jsonParser.parseMap(response).get("access_token").toString();
        log.info("Extracted access token from the response");
        return accessToken;
    }

    private void warnAboutMissingSecrets() {
        if (StringUtils.isBlank(clientId)) {
            log.warn("auth.homeoffice.client.id is null or empty. If you're connecting to a mock API ignore"
                + " this warning. If you're in a production environment, you may have a problem.");
        }
        if (StringUtils.isBlank(clientSecret)) {
            log.warn("auth.homeoffice.client.secret is null or empty. If you're connecting to a mock API ignore"
                + " this warning. If you're in a production environment, you may have a problem.");

        }
    }
}
