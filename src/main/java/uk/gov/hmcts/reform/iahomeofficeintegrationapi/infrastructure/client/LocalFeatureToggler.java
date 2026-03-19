package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.FeatureToggler;

@Service
@Profile("local")
public class LocalFeatureToggler implements FeatureToggler {

    @Override
    public boolean getValue(String key, Boolean defaultValue) {
        // Always return the default value locally
        return defaultValue;
    }
}