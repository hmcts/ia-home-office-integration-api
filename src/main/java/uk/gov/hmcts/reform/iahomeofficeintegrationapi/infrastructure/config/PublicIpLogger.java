package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@Slf4j
@Component
@ConditionalOnProperty(
    name = "app.public-ip.enabled",
    havingValue = "true",
    matchIfMissing = false
)
public class PublicIpLogger implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) {

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(2000);

        RestTemplate restTemplate = new RestTemplate(factory);

        try {
            String ip = restTemplate.getForObject("https://api.ipify.org", String.class);
            log.info("Application started with public IP: {}", ip);
        } catch (Exception e) {
            log.warn("Could not determine public IP address: {}", e.getMessage());
        }
    }
}