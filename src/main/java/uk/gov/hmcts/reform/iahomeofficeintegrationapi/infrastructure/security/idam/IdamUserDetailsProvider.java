package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security.idam;

import feign.FeignException;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.IdamService;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.IdamApi;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.model.idam.UserInfo;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security.AccessTokenProvider;

public class IdamUserDetailsProvider implements UserDetailsProvider {

    private final AccessTokenProvider accessTokenProvider;
    private final IdamApi idamApi;
    private final IdamService idamService;

    public IdamUserDetailsProvider(
        AccessTokenProvider accessTokenProvider,
        IdamApi idamApi,
        IdamService idamService
    ) {

        this.accessTokenProvider = accessTokenProvider;
        this.idamApi = idamApi;
        this.idamService = idamService;
    }

    public IdamUserDetails getUserDetails() {

        final String accessToken = accessTokenProvider.getAccessToken();

        UserInfo response;

        try {
            response = idamService.getUserInfo(accessToken);
        } catch (FeignException ex) {
            throw new IdentityManagerResponseException(
                "Could not get user details with IDAM",
                ex
            );
        }

        if (response == null) {
            throw new IdentityManagerResponseException(
                "Could not get user details with IDAM", null
            );
        }

        if (response.getUid() == null) {
            throw new IllegalStateException("IDAM user details missing 'uid' field");
        }

        if (response.getRoles() == null) {
            throw new IllegalStateException("IDAM user details missing 'roles' field");
        }

        if (response.getEmail() == null) {
            throw new IllegalStateException("IDAM user details missing 'sub' field");
        }

        if (response.getGivenName() == null) {
            throw new IllegalStateException("IDAM user details missing 'given_name' field");
        }

        if (response.getFamilyName() == null) {
            throw new IllegalStateException("IDAM user details missing 'family_name' field");
        }

        return new IdamUserDetails(
            accessToken,
            response.getUid(),
            response.getRoles(),
            response.getEmail(),
            response.getGivenName(),
            response.getFamilyName()
        );
    }

}
