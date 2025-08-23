package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import com.microsoft.applicationinsights.boot.dependencies.apachecommons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Getter
@Setter
public class ConfigValidatorAppListener implements ApplicationListener<ContextRefreshedEvent> {

    Environment env;

    @Value("${auth.homeoffice.client.baseUrl}")
    private String homeOfficeBaseUrl;
    @Value("${auth.homeoffice.client.id}")
    private String clientId;
    @Value("${auth.homeoffice.client.secret}")
    private String clientSecret;

    @Autowired
    public ConfigValidatorAppListener(Environment env) {
        this.env = env;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        breakOnMissingHomeOfficeSecrets();
    }

    void breakOnMissingHomeOfficeSecrets() {
        if (homeOfficeBaseUrl.contains("localhost") || homeOfficeBaseUrl.contains("127.0.0.1")) {
            log.warn("Detected local deployment. Secrets will not be checked.");
            return;
        }
        log.info("Home office base URL: {}", homeOfficeBaseUrl);
        if (StringUtils.isBlank(clientId)) {
            throw new IllegalArgumentException("auth.homeoffice.client.id is null or empty."
                + " This is not allowed and it will break production. This is a secret value stored in a vault"
                + " (unless running locally). Check application.yaml for further information.");
        }
        if (StringUtils.isBlank(clientSecret)) {
            log.info("Home office base URL: {}", homeOfficeBaseUrl);
            throw new IllegalArgumentException("auth.homeoffice.client.secret is null or empty."
                + " This is not allowed and it will break production. This is a secret value stored in a vault"
                + " (unless running locally). Check application.yaml for further information.");

        }
    }
}
