package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
public class S2SEndpointAuthorizationFilter extends OncePerRequestFilter {

    private static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    private static final String BEARER = "Bearer ";

    // Define which endpoints Home Office can access
    private static final List<String> HOME_OFFICE_ALLOWED_ENDPOINTS = List.of(
        "/home-office-statutory-timeframe-status"
    );

    // Define Home Office service names
    private static final List<String> HOME_OFFICE_SERVICES = List.of(
        "home-office-immigration"
    );

    private final AuthTokenValidator authTokenValidator;

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

            // If service is Home Office, check if endpoint is allowed
            if (HOME_OFFICE_SERVICES.contains(serviceName)) {
                if (!isHomeOfficeAllowedToAccessEndpoint(requestPath)) {
                    log.error("Home Office service '{}' is not authorised to access endpoint: {}",
                        serviceName, requestPath);
                    throw new AccessDeniedException(
                        "Service '" + serviceName + "' is not authorised to access endpoint: " + requestPath
                    );
                }
                log.info("Home Office service '{}' is authorised to access endpoint: {}",
                    serviceName, requestPath);
            } else {
                // Ministry of Justice services can access all endpoints
                log.info("Ministry of Justice service '{}' is authorised to access all endpoints", serviceName);
            }

            filterChain.doFilter(request, response);

        } catch (AccessDeniedException e) {
            log.error("Access denied: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        }
    }

    private boolean isHomeOfficeAllowedToAccessEndpoint(String requestPath) {
        return HOME_OFFICE_ALLOWED_ENDPOINTS.stream()
            .anyMatch(requestPath::startsWith);
    }

    private String getServiceName(String authHeader) {
        String bearerAuthToken = authHeader.startsWith(BEARER) ? authHeader : BEARER.concat(authHeader);
        return authTokenValidator.getServiceName(bearerAuthToken);
    }
}
