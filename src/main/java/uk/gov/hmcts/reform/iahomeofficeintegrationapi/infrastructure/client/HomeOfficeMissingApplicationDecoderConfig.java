package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.databind.ObjectMapper;

public class HomeOfficeMissingApplicationDecoderConfig {

    @Bean
    public ErrorDecoder homeOfficeMissingApplicationDecoder(ObjectMapper objectMapper) {
        return new HomeOfficeMissingApplicationDecoder(objectMapper);
    }
}