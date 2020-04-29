package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security.RequestUserAccessTokenProvider;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security.idam.IdamUserDetailsProvider;


@Configuration
public class UserDetailsProviderConfiguration {

    @Bean("requestUser")
    @Primary
    public UserDetailsProvider getRequestUserDetailsProvider(
        RequestUserAccessTokenProvider requestUserAccessTokenProvider,
        RestTemplate restTemplate,
        @Value("${auth.idam.client.baseUrl}") String baseUrl,
        @Value("${auth.idam.client.detailsUri}") String detailsUri
    ) {
        return new IdamUserDetailsProvider(
            requestUserAccessTokenProvider,
            restTemplate,
            baseUrl,
            detailsUri
        );
    }
}
