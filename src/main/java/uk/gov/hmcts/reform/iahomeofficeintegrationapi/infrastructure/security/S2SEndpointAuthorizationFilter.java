package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("#{'${security.anonymousPaths:}'.split(',')}")
    private List<String> anonymousPaths;

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

        // Skip S2S validation for anonymous paths
        if (isAnonymousPath(requestPath)) {
            log.warn("Skipping S2S validation for anonymous path: {}", requestPath);
            filterChain.doFilter(request, response);
            return;
        }

        String s2sToken = request.getHeader(SERVICE_AUTHORIZATION_HEADER);

        if (s2sToken == null || s2sToken.isEmpty()) {
            log.info("S2S authentication failed for {} {}: Missing ServiceAuthorization header",
                request.getMethod(), requestPath);
            sendUnauthorizedResponse(response, "Missing ServiceAuthorization header (S2S token required)");
            return;
        }

        String serviceName;
        try {
            serviceName = getServiceName(s2sToken);
        } catch (Exception e) {
            log.info("S2S authentication failed for {} {}: Invalid or expired S2S token - {}",
                request.getMethod(), requestPath, e.getMessage());
            sendUnauthorizedResponse(response, "Invalid or expired S2S token");
            return;
        }
        log.debug("S2S service '{}' attempting to access endpoint: {}", serviceName, requestPath);

        boolean isKnownService = false;
        boolean isEndpointAllowed = false;

        // Check IAC service group
        if (iacServices.contains(serviceName)) {
            isKnownService = true;
            log.debug("Service '{}' identified as IAC service", serviceName);
            log.debug("IAC allowed endpoints: {}", iacAllowedEndpoints);
            if (isEndpointAllowed(requestPath, iacAllowedEndpoints)) {
                isEndpointAllowed = true;
                log.debug("Endpoint '{}' allowed via IAC service group", requestPath);
            }
        }

        // Check Home Office service group
        if (homeOfficeServices.contains(serviceName)) {
            isKnownService = true;
            log.debug("Service '{}' identified as Home Office service", serviceName);
            log.debug("Home Office allowed endpoints: {}", homeOfficeAllowedEndpoints);
            if (isEndpointAllowed(requestPath, homeOfficeAllowedEndpoints)) {
                isEndpointAllowed = true;
                log.debug("Endpoint '{}' allowed via Home Office service group", requestPath);
            }
        }

        if (!isKnownService) {
            log.info("S2S authentication failed: Service '{}' is not a known/authorised service", serviceName);
            sendUnauthorizedResponse(response, "Service '" + serviceName + "' is not a recognised service");
            return;
        }

        if (!isEndpointAllowed) {
            log.info("Access DENIED: Service '{}' is not authorised to access endpoint '{}'",
                serviceName, requestPath);
            sendForbiddenResponse(response, "Service '" + serviceName + "' is not authorised to access endpoint: " + requestPath);
            return;
        }

        log.debug("Access GRANTED: Service '{}' is authorised to access endpoint '{}'", serviceName, requestPath);
        filterChain.doFilter(request, response);
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
        response.getWriter().flush();
    }

    private void sendForbiddenResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
        response.getWriter().flush();
    }

    private boolean isEndpointAllowed(String requestPath, List<String> allowedEndpoints) {
        return allowedEndpoints.stream()
            .anyMatch(requestPath::startsWith);
    }

    private String getServiceName(String authHeader) {
        String bearerAuthToken = authHeader.startsWith(BEARER) ? authHeader : BEARER.concat(authHeader);
        return authTokenValidator.getServiceName(bearerAuthToken);
    }

    private boolean isAnonymousPath(String requestPath) {
        return anonymousPaths.stream()
            .filter(path -> path != null && !path.trim().isEmpty())
            .anyMatch(path -> requestPath.equals(path.trim()) || requestPath.startsWith(path.trim() + "/"));
    }
}
