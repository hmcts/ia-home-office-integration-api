package uk.gov.hmcts.reform.iahomeofficeintegrationapi.util;

import io.restassured.http.Header;
import io.restassured.http.Headers;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.IdamApi;

@Service
public class AuthorizationHeadersProvider {

    private final Map<String, String> tokens = new ConcurrentHashMap<>();
    @Value("${idam.redirectUrl}")
    protected String idamRedirectUrl;
    @Value("${idam.scope}")
    protected String userScope;
    @Value("${spring.security.oauth2.client.registration.oidc.client-id}")
    protected String idamClientId;
    @Value("${spring.security.oauth2.client.registration.oidc.client-secret}")
    protected String idamClientSecret;
    @Autowired
    private AuthTokenGenerator serviceAuthTokenGenerator;
    @Autowired
    private IdamApi idamApi;

    public Headers getLegalRepresentativeAuthorization() {

        return getAuthorizationForRole(
            "LegalRepresentative",
            "TEST_LAW_FIRM_A_USERNAME",
            "TEST_LAW_FIRM_A_PASSWORD");

    }

    public Headers getCaseOfficerAuthorization() {

        return getAuthorizationForRole(
            "CaseOfficer",
            "TEST_CASEOFFICER_USERNAME",
            "TEST_CASEOFFICER_PASSWORD");
    }

    public Headers getAdminOfficerAuthorization() {

        return getAuthorizationForRole(
            "AdminOfficer",
            "TEST_ADMINOFFICER_USERNAME",
            "TEST_ADMINOFFICER_PASSWORD");
    }

    public Headers getJudgeAuthorization() {

        return getAuthorizationForRole(
            "Judge",
            "TEST_JUDGE_X_USERNAME",
            "TEST_JUDGE_X_PASSWORD");

    }

    public Headers getHomeOfficePouAuthorization() {

        return getAuthorizationForRole(
            "HomeOfficePou",
            "TEST_HOMEOFFICE_POU_USERNAME",
            "TEST_HOMEOFFICE_POU_PASSWORD");
    }

    public Headers getHomeOfficeGenericAuthorization() {

        return getAuthorizationForRole(
            "HomeOfficeGeneric",
            "TEST_HOMEOFFICE_GENERIC_USERNAME",
            "TEST_HOMEOFFICE_GENERIC_PASSWORD");
    }

    public Headers getAuthorizationForRole(String role, String username, String password) {

        MultiValueMap<String, String> tokenRequestForm = new LinkedMultiValueMap<>();
        tokenRequestForm.add("grant_type", "password");
        tokenRequestForm.add("redirect_uri", idamRedirectUrl);
        tokenRequestForm.add("client_id", idamClientId);
        tokenRequestForm.add("client_secret", idamClientSecret);
        tokenRequestForm.add("username", System.getenv(username));
        tokenRequestForm.add("password", System.getenv(password));
        tokenRequestForm.add("scope", userScope);

        String serviceToken = tokens.computeIfAbsent("ServiceAuth", user -> serviceAuthTokenGenerator.generate());
        String accessToken = tokens.computeIfAbsent(
            role,
            user -> "Bearer " + idamApi.token(tokenRequestForm).getAccessToken()
        );

        return new Headers(
            new Header("ServiceAuthorization", serviceToken),
            new Header("Authorization", accessToken)
        );
    }
}
