package uk.gov.hmcts.reform.iahomeofficeintegrationapi.util;

import io.restassured.http.Header;
import io.restassured.http.Headers;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

@Service
public class AuthorizationHeadersProvider {

    private final Map<String, String> tokens = new ConcurrentHashMap<>();

    @Autowired
    private AuthTokenGenerator serviceAuthTokenGenerator;
    @Autowired
    private IdamAuthProvider idamAuthProvider;

    public Headers getLegalRepresentativeAuthorization() {
        String serviceToken = tokens.computeIfAbsent("ServiceAuth", user -> serviceAuthTokenGenerator.generate());
        String accessToken = idamAuthProvider.getLegalRepToken();

        return new Headers(
            new Header("ServiceAuthorization", serviceToken),
            new Header("Authorization", accessToken)
        );
    }

    public Headers getCaseOfficerAuthorization() {
        String serviceToken = tokens.computeIfAbsent("ServiceAuth", user -> serviceAuthTokenGenerator.generate());
        String accessToken = idamAuthProvider.getCaseOfficerToken();

        return new Headers(
            new Header("ServiceAuthorization", serviceToken),
            new Header("Authorization", accessToken)
        );
    }

    public Headers getAdminOfficerAuthorization() {
        String serviceToken = tokens.computeIfAbsent("ServiceAuth", user -> serviceAuthTokenGenerator.generate());
        String accessToken = idamAuthProvider.getAdminOfficerToken();

        return new Headers(
            new Header("ServiceAuthorization", serviceToken),
            new Header("Authorization", accessToken)
        );
    }

    public Headers getJudgeAuthorization() {
        String serviceToken = tokens.computeIfAbsent("ServiceAuth", user -> serviceAuthTokenGenerator.generate());
        String accessToken = idamAuthProvider.getJudgeToken();

        return new Headers(
            new Header("ServiceAuthorization", serviceToken),
            new Header("Authorization", accessToken)
        );
    }

    public Headers getHomeOfficePouAuthorization() {
        String serviceToken = tokens.computeIfAbsent("ServiceAuth", user -> serviceAuthTokenGenerator.generate());
        String accessToken = idamAuthProvider.getHomeOfficePouToken();

        return new Headers(
            new Header("ServiceAuthorization", serviceToken),
            new Header("Authorization", accessToken)
        );
    }

    public Headers getHomeOfficeGenericAuthorization() {
        String serviceToken = tokens.computeIfAbsent("ServiceAuth", user -> serviceAuthTokenGenerator.generate());
        String accessToken = idamAuthProvider.getHomeOfficeGenericToken();

        return new Headers(
            new Header("ServiceAuthorization", serviceToken),
            new Header("Authorization", accessToken)
        );
    }
}
