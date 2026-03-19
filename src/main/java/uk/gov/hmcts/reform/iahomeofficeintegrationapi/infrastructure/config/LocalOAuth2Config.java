package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

@Configuration
@Profile("local")
public class LocalOAuth2Config {

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        // returns a dummy repository so Spring can wire springSecurityFilterChain
        return registration -> null;
    }
}