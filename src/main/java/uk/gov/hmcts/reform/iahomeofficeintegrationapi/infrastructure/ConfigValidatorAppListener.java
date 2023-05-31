package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Getter
@Setter
public class ConfigValidatorAppListener implements ApplicationListener<ContextRefreshedEvent> {

    @Value("${auth.homeoffice.client.baseUrl}")
    private String homeOfficeBaseUrl;
    @Value("${auth.homeoffice.client.id}")
    private String clientId;
    @Value("${auth.homeoffice.client.secret}")
    private String clientSecret;


    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        breakOnMissingHomeOfficeSecrets();
    }

    void breakOnMissingHomeOfficeSecrets() {
        if (StringUtils.isBlank(clientId)) {
            log.info("Home office base URL: {}", homeOfficeBaseUrl);
            printEnvironment();
            throw new IllegalArgumentException("auth.homeoffice.client.id is null or empty."
                + " This is not allowed and it will break production. This is a secret value stored in a vault"
                + " (unless running locally). Check application.yaml for further information.");
        }
        if (StringUtils.isBlank(clientSecret)) {
            log.info("Home office base URL: {}", homeOfficeBaseUrl);
            printEnvironment();
            throw new IllegalArgumentException("auth.homeoffice.client.secret is null or empty."
                + " This is not allowed and it will break production. This is a secret value stored in a vault"
                + " (unless running locally). Check application.yaml for further information.");

        }
    }

    private void printEnvironment() {
        log.info("Environment variables: ");
        Map<String, String> environment = System.getenv();

        for (Map.Entry<String, String> entry : environment.entrySet()) {
            log.info("    {} = {}", entry.getKey(), entry.getValue());
        }
    }
}
