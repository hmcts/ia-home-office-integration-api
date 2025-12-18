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
            log.info("Home Office allowed services: {}", homeOfficeServices);
            log.info("Home Office allowed endpoints: {}", homeOfficeAllowedEndpoints);

            // If service is Home Office, check if endpoint is allowed
            if (homeOfficeServices.contains(serviceName)) {
                log.info("Service '{}' identified as Home Office service", serviceName);
                if (!isHomeOfficeAllowedToAccessEndpoint(requestPath)) {
                    log.error("Access DENIED: Home Office service '{}' is not authorised to access endpoint '{}'. Allowed endpoints: {}",
                        serviceName, requestPath, homeOfficeAllowedEndpoints);
                    throw new AccessDeniedException(
                        "Service '" + serviceName + "' is not authorised to access endpoint: " + requestPath
                    );
                }
                log.info("Access GRANTED: Home Office service '{}' is authorised to access endpoint '{}'",
                    serviceName, requestPath);
            } else {
                // Ministry of Justice services can access all endpoints
                log.info("Access GRANTED: Ministry of Justice service '{}' is authorised to access all endpoints", serviceName);
            }

            filterChain.doFilter(request, response);

        } catch (AccessDeniedException e) {
            log.error("Access denied for endpoint '{}': {}", requestPath, e.getMessage());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            log.error("Error validating S2S token for endpoint '{}': {}", requestPath, e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid S2S token");
        }
    }

    private boolean isHomeOfficeAllowedToAccessEndpoint(String requestPath) {
        return homeOfficeAllowedEndpoints.stream()
            .anyMatch(requestPath::startsWith);
    }

    private String getServiceName(String authHeader) {
        String bearerAuthToken = authHeader.startsWith(BEARER) ? authHeader : BEARER.concat(authHeader);
        return authTokenValidator.getServiceName(bearerAuthToken);
    }
}
