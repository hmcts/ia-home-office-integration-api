package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.config;

import java.util.HashMap;
import java.util.Map;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties
@ConfigurationProperties("homeoffice.params")
public class HomeOfficeProperties {

    private Map<String, LookupReferenceData> homeOfficeReferenceData = new HashMap<>();

    public Map<String, LookupReferenceData> getHomeOfficeReferenceData() {
        return homeOfficeReferenceData;
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
