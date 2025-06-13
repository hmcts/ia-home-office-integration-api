package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.IdamApi;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.model.idam.UserInfo;

@Component
public class IdamService {

    private final IdamApi idamApi;

    public IdamService(IdamApi idamApi) {
        this.idamApi = idamApi;
    }

    @Cacheable(value = "userInfoCache")
    public UserInfo getUserInfo(String accessToken) {
        return idamApi.userInfo(accessToken);
    }
}

