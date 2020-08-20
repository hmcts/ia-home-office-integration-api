package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security.homeoffice.HomeOfficeAuthorizor;


@Service
@Qualifier("homeOffice")
public class HomeOfficeAccessTokenProvider implements AccessTokenProvider {

    private final HomeOfficeAuthorizor homeOfficeAuthorizor;

    public HomeOfficeAccessTokenProvider(HomeOfficeAuthorizor homeOfficeAuthorizor) {
        this.homeOfficeAuthorizor = homeOfficeAuthorizor;
    }

    public String getAccessToken() {
        return tryGetAccessToken()
            .orElseThrow(() -> new IllegalStateException("System access token not present"));
    }

    public Optional<String> tryGetAccessToken() {

        return Optional.ofNullable(
            homeOfficeAuthorizor.fetchCodeAuthorization()
        );
    }
}
