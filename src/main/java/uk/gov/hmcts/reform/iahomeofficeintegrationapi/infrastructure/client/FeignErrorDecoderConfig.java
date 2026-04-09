package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.databind.ObjectMapper;

public class FeignErrorDecoderConfig {

    @Bean
    public ErrorDecoder homeOfficeErrorDecoder(ObjectMapper objectMapper) {
        return new FeignErrorDecoder(objectMapper);
    }
}