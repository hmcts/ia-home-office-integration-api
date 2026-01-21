package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class S2SEndpointAuthorizationFilter extends OncePerRequestFilter {

    private static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    private static final String BEARER = "Bearer ";

    @Value("#{'${idam.s2s-authorised.iac-services.allowed-endpoints}'.split(',')}")
    private List<String> iacAllowedEndpoints;

    @Value("#{'${idam.s2s-authorised.iac-services.allowed-services}'.split(',')}")
    private List<String> iacServices;

    @Value("#{'${idam.s2s-authorised.home-office-immigration.allowed-endpoints}'.split(',')}")
    private List<String> homeOfficeAllowedEndpoints;

    @Value("#{'${idam.s2s-authorised.home-office-immigration.allowed-services}'.split(',')}")
    private List<String> homeOfficeServices;

    private final AuthTokenValidator authTokenValidator;

    public S2SEndpointAuthorizationFilter(AuthTokenValidator authTokenValidator) {
        this.authTokenValidator = authTokenValidator;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        String requestPath = request.getRequestURI();
        String s2sToken = request.getHeader(SERVICE_AUTHORIZATION_HEADER);

        if (s2sToken == null) {
            log.info("No S2S token found for request to {}", requestPath);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String serviceName = getServiceName(s2sToken);
            log.info("S2S service '{}' attempting to access endpoint: {}", serviceName, requestPath);

            boolean isKnownService = false;
            boolean isEndpointAllowed = false;

            // Check IAC service group
            if (iacServices.contains(serviceName)) {
                isKnownService = true;
                log.info("Service '{}' identified as IAC service", serviceName);
                log.info("IAC allowed endpoints: {}", iacAllowedEndpoints);
                if (isEndpointAllowed(requestPath, iacAllowedEndpoints)) {
                    isEndpointAllowed = true;
                    log.info("Endpoint '{}' allowed via IAC service group", requestPath);
                }
            }

            // Check Home Office service group
            if (homeOfficeServices.contains(serviceName)) {
                isKnownService = true;
                log.info("Service '{}' identified as Home Office service", serviceName);
                log.info("Home Office allowed endpoints: {}", homeOfficeAllowedEndpoints);
                if (isEndpointAllowed(requestPath, homeOfficeAllowedEndpoints)) {
                    isEndpointAllowed = true;
                    log.info("Endpoint '{}' allowed via Home Office service group", requestPath);
                }
            }

            if (!isKnownService) {
                log.error("Access DENIED: Service '{}' is not a known/authorised service", serviceName);
                throw new AccessDeniedException(
                    "Service '" + serviceName + "' is not authorised to access this endpoint: " + requestPath
                );
            }

            if (!isEndpointAllowed) {
                log.error("Access DENIED: Service '{}' is not authorised to access endpoint '{}'",
                    serviceName, requestPath);
                throw new AccessDeniedException(
                    "Service '" + serviceName + "' is not authorised to access endpoint: " + requestPath
                );
            }

            log.info("Access GRANTED: Service '{}' is authorised to access endpoint '{}'", serviceName, requestPath);
            filterChain.doFilter(request, response);

        } catch (AccessDeniedException e) {
            log.error("Access denied for endpoint '{}': {}", requestPath, e.getMessage());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            log.error("Error validating S2S token for endpoint '{}': {}", requestPath, e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid S2S token");
        }
    }

    private boolean isEndpointAllowed(String requestPath, List<String> allowedEndpoints) {
        return allowedEndpoints.stream()
            .anyMatch(requestPath::startsWith);
    }

    private String getServiceName(String authHeader) {
        String bearerAuthToken = authHeader.startsWith(BEARER) ? authHeader : BEARER.concat(authHeader);
        return authTokenValidator.getServiceName(bearerAuthToken);
    }
}
