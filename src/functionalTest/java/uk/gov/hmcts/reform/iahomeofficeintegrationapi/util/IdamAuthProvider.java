package uk.gov.hmcts.reform.iahomeofficeintegrationapi.util;

import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.IdamApi;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.model.idam.Token;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.model.idam.UserInfo;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security.idam.IdentityManagerResponseException;

@Service
public class IdamAuthProvider {

    @Value("${idam.redirectUrl}")
    protected String idamRedirectUri;

    @Value("${idam.scope}") 
    protected String userScope;

    @Value("${spring.security.oauth2.client.registration.oidc.client-id}")
    protected String idamClientId;

    @Value("${spring.security.oauth2.client.registration.oidc.client-secret}")
    protected String idamClientSecret;

    @Autowired
    private IdamApi idamApi;

    public String getUserToken(String username, String password) {

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "password");
        map.add("redirect_uri", idamRedirectUri);
        map.add("client_id", idamClientId);
        map.add("client_secret", idamClientSecret);
        map.add("username", username);
        map.add("password", password);
        map.add("scope", userScope);
        try {
            Token tokenResponse = idamApi.token(map);
            return "Bearer " + tokenResponse.getAccessToken();
        } catch (FeignException ex) {
            throw new IdentityManagerResponseException("Could not get user token from IDAM", ex);
        }
    }

    @Cacheable(value = "legalRepATokenCache")
    public String getLegalRepToken() {
        return getUserToken(
            System.getenv("TEST_LAW_FIRM_A_USERNAME"),
            System.getenv("TEST_LAW_FIRM_A_PASSWORD")
        );
    }

    @Cacheable(value = "caseOfficerTokenCache")
    public String getCaseOfficerToken() {
        return getUserToken(
            System.getenv("TEST_CASEOFFICER_USERNAME"),
            System.getenv("TEST_CASEOFFICER_PASSWORD")
        );
    }

    @Cacheable(value = "adminOfficerTokenCache")
    public String getAdminOfficerToken() {
        return getUserToken(
            System.getenv("TEST_ADMINOFFICER_USERNAME"),
            System.getenv("TEST_ADMINOFFICER_PASSWORD")
        );
    }

    @Cacheable(value = "homeOfficePouTokenCache")
    public String getHomeOfficePouToken() {
        return getUserToken(
            System.getenv("TEST_HOMEOFFICE_POU_USERNAME"),
            System.getenv("TEST_HOMEOFFICE_POU_PASSWORD")
        );
    }

    @Cacheable(value = "homeOfficeGenericTokenCache")
    public String getHomeOfficeGenericToken() {
        return getUserToken(
            System.getenv("TEST_HOMEOFFICE_GENERIC_USERNAME"),
            System.getenv("TEST_HOMEOFFICE_GENERIC_PASSWORD")
        );
    }


    @Cacheable(value = "judgeTokenCache")
    public String getJudgeToken() {
        return getUserToken(
            System.getenv("TEST_JUDGE_X_USERNAME"),
            System.getenv("TEST_JUDGE_X_PASSWORD")
        );
    }

    public String getUserId(String token) {
        try {
            UserInfo userInfo = idamApi.userInfo(token);
            return userInfo.getUid();
        } catch (FeignException ex) {
            throw new IdentityManagerResponseException("Could not get system user token from IDAM", ex);
        }
    }
}
