package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class S2STokenValidator {

    private static final String BEARER = "Bearer ";

    private final AuthTokenValidator authTokenValidator;

    public void checkIfServiceIsAllowed(String token, List<String> allowedServices) {
        String serviceName = authenticate(token);
        if (!Objects.nonNull(serviceName)) {
            log.info("Service name from S2S token is null");
            throw new AccessDeniedException("Service name from S2S token ('ServiceAuthorization' header) is null");
        }
        log.debug("S2S token validated for service: {}", serviceName);
        if (!allowedServices.contains(serviceName)) {
            log.info("Service name '{}' is not allowed to access this endpoint. Allowed services: {}", serviceName, allowedServices);
            throw new AccessDeniedException("Service '" + serviceName + "' is not authorised to access this endpoint.");
        }
        log.debug("Service '{}' is authorised for this endpoint", serviceName);
    }

    private String authenticate(String authHeader) {
        String bearerAuthToken = getBearerToken(authHeader);
        return authTokenValidator.getServiceName(bearerAuthToken);
    }

    private String getBearerToken(String token) {
        return token.startsWith(BEARER) ? token : BEARER.concat(token);
    }

}
