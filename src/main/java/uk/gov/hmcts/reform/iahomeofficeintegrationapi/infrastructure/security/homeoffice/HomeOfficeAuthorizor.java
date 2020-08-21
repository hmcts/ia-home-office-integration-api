package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security.homeoffice;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
            log.info("Received JWT token response from Home Office: " + response);

        } catch (RestClientResponseException e) {

            throw new IdentityManagerResponseException(
                "Could not get auth code with Home Office",
                e
            );
        }
        return "Bearer " + extractAccessToken(response);
    }

    private String extractAccessToken(String response) {

        DecodedJWT decodedJwt;
        try {
            decodedJwt = JWT.decode(response);
        } catch (JWTDecodeException e) {
            //Invalid token
            e.printStackTrace();
            throw new IdentityManagerResponseException(
                "Exception decoding the token: " + e.getMessage(), e
            );
        }
        return decodedJwt.getClaims().getOrDefault("access_token", null).asString();
    }
}
