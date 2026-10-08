package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@Configuration
@Slf4j
@SuppressWarnings("removal")
public class RestTemplateConfiguration {

    @Bean
    public RestOperations restOperations(
            ObjectMapper objectMapper
    ) {
        return restTemplate(objectMapper);
    }

    @Bean
    public RestTemplate restTemplate(ObjectMapper objectMapper) {
        RestTemplate restTemplate = new RestTemplate();

        int jackson3Index = -1;
        for (int i = 0; i < restTemplate.getMessageConverters().size(); i++) {
            HttpMessageConverter<?> converter = restTemplate.getMessageConverters().get(i);
            log.info("BEFORE converter: {}", converter.getClass().getName());
            if (converter.getClass().getName()
                    .startsWith("org.springframework.http.converter.json.")
                    && converter.getClass().getSimpleName().contains("Jackson")) {
                jackson3Index = i;
            }
        }

        restTemplate.getMessageConverters().remove(jackson3Index);

        restTemplate.getMessageConverters().add(jackson3Index,
                new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter(objectMapper)
        );

        log.info("AFTER");
        log.info("modules: {}, inclusion: {}",
                objectMapper.getRegisteredModuleIds(),
                objectMapper.getSerializationConfig().getDefaultPropertyInclusion());

        restTemplate.getMessageConverters()
                .forEach(c -> log.info("AFTER converter: {}", c.getClass().getName()));

        return restTemplate;
    }

}
