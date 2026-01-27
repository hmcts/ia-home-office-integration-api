package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGeneratorFactory;

@Configuration
@Lazy
@Slf4j
public class ServiceTokenGeneratorConfiguration {

    @Bean
    public AuthTokenGenerator authTokenGenerator(
        @Value("${idam.s2s-auth.totp_secret}") String secret,
        @Value("${idam.s2s-auth.microservice}") String microService,
        @Value("${idam.s2s-auth.url}") String s2sUrl,
        ServiceAuthorisationApi serviceAuthorisationApi
    ) {
        log.info("S2S auth URL: {}", s2sUrl); // DO NOT MERGE THIS
        log.info("S2S microservice: {}", microService);
        return AuthTokenGeneratorFactory.createDefaultGenerator(
            secret,
            microService,
            serviceAuthorisationApi
        );
    }
}
