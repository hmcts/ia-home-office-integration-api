package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.config;

import java.util.HashMap;
import java.util.Map;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties
@ConfigurationProperties("home-office.params")
public class HomeOfficeProperties {

    private Map<String, LookupReferenceData> codes = new HashMap<>();

    public Map<String, LookupReferenceData> getCodes() {
        return codes;
    }

    public static class LookupReferenceData {
        @Setter
        private String code;
        @Setter
        private String description;

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

    }

}
