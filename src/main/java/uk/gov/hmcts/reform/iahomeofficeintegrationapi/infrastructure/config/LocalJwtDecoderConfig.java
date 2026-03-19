package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Instant;
import java.util.Collections;

@Configuration
@Profile("local")
public class LocalJwtDecoderConfig {

    @Bean
    public JwtDecoder jwtDecoder() {
        return token -> new Jwt(
                "dummy-token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Collections.emptyMap(),
                Collections.emptyMap()
        );
    }
}