package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class HomeOfficeMissingApplicationDecoderConfig {

    @Bean
    public ErrorDecoder homeOfficeMissingApplicationDecoder(ObjectMapper objectMapper) {
        return new HomeOfficeMissingApplicationDecoder(objectMapper);
    }
}