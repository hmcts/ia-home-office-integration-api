package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${idam.s2s-authorised.services}")
    private final List<String> iaS2sAuthorisedServices;

    private final AuthTokenValidator authTokenValidator;

    public void checkIfServiceIsAllowed(String token) {
        log.info("Validating S2S token for service authentication. Token: {}", token);
        String serviceName = authenticate(token);
        if (!Objects.nonNull(serviceName)) {
            log.error("Service name from S2S token is null");
            throw new AccessDeniedException("Service name from S2S token ('ServiceAuthorization' header) is null");
        }
        log.info("S2S token validated for service: {}", serviceName);
        log.info("Authorised services: {}", iaS2sAuthorisedServices);
        if (!iaS2sAuthorisedServices.contains(serviceName)) {
            log.error("Service name '{}' was not recognised for S2S authentication. Please check s2s-authorised.services in application.yaml", serviceName);
            throw new AccessDeniedException("Service name from S2S token ('ServiceAuthorization' header) is not recognised.");
        }
        log.info("Service '{}' is authorised", serviceName);
    }

    public void checkIfServiceIsAllowed(String token, List<String> allowedServices) {
        log.info("Validating S2S token for service authentication. Token: {}", token);
        String serviceName = authenticate(token);
        if (!Objects.nonNull(serviceName)) {
            log.error("Service name from S2S token is null");
            throw new AccessDeniedException("Service name from S2S token ('ServiceAuthorization' header) is null");
        }
        log.info("S2S token validated for service: {}", serviceName);
        log.info("Allowed services for this endpoint: {}", allowedServices);
        if (!allowedServices.contains(serviceName)) {
            log.error("Service name '{}' is not allowed to access this endpoint. Allowed services: {}", serviceName, allowedServices);
            throw new AccessDeniedException("Service '" + serviceName + "' is not authorised to access this endpoint.");
        }
        log.info("Service '{}' is authorised for this endpoint", serviceName);
    }

    private String authenticate(String authHeader) {
        String bearerAuthToken = getBearerToken(authHeader);
        log.info("Bearer auth token: {}", bearerAuthToken);
        return authTokenValidator.getServiceName(bearerAuthToken);
    }

    private String getBearerToken(String token) {
        return token.startsWith(BEARER) ? token : BEARER.concat(token);
    }

}
