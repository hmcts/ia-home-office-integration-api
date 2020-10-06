package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class UuidIdentityProvider implements IdentityProvider {

    public String identity() {
        return UUID.randomUUID().toString();
    }
}
