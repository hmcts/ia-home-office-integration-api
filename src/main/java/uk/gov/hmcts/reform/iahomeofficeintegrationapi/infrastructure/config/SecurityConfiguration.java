package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.config;

import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import com.google.common.collect.ImmutableMap;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security.AuthorizedRolesProvider;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security.CcdEventAuthorizor;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security.S2SEndpointAuthorizationFilter;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security.SpringAuthorizedRolesProvider;

@Slf4j
@Configuration
@ConfigurationProperties(prefix = "security")
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfiguration {

    private final List<String> anonymousPaths = new ArrayList<>();

    private final Map<String, List<Event>> roleEventAccess = new HashMap<>();

    private final Converter<Jwt, Collection<GrantedAuthority>> idamAuthoritiesConverter;
    private final S2SEndpointAuthorizationFilter s2SEndpointAuthorizationFilter;

    public SecurityConfiguration(Converter<Jwt, Collection<GrantedAuthority>> idamAuthoritiesConverter,
                                 S2SEndpointAuthorizationFilter s2SEndpointAuthorizationFilter) {
        this.idamAuthoritiesConverter = idamAuthoritiesConverter;
        this.s2SEndpointAuthorizationFilter = s2SEndpointAuthorizationFilter;
    }

    public List<String> getAnonymousPaths() {
        return anonymousPaths;
    }

    @Bean
    WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> {
            web.ignoring().requestMatchers(anonymousPaths
                .stream()
                .toArray(String[]::new)
            );
        };
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(idamAuthoritiesConverter);

        http
            .addFilterAfter(s2SEndpointAuthorizationFilter, BearerTokenAuthenticationFilter.class)
            .sessionManagement(management -> management.sessionCreationPolicy(STATELESS))
            .exceptionHandling(handling -> handling
                .authenticationEntryPoint((request, response, authException) -> {
                    String authHeader = request.getHeader("Authorization");
                    String message;
                    if (authHeader == null || authHeader.isEmpty()) {
                        message = "Missing Authorization header (Bearer token required)";
                    } else if (!authHeader.startsWith("Bearer ")) {
                        message = "Invalid Authorization header format (Bearer token required)";
                    } else {
                        message = "Invalid or expired Authorization token";
                    }
                    log.info("JWT authentication failed for {} {}: {}",
                        request.getMethod(), request.getRequestURI(), message);
                    response.setStatus(401);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"" + message + "\"}");
                    response.getWriter().flush();
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    log.info("Access denied for request to {}: {}",
                        request.getRequestURI(), accessDeniedException.getMessage());
                    response.sendError(403, accessDeniedException.getMessage());
                }))
            .csrf(csrf -> csrf.disable())
            .formLogin(login -> login.disable())
            .logout(logout -> logout.disable())
            .authorizeHttpRequests(requests -> requests
                .anyRequest().authenticated())
            .oauth2ResourceServer(server -> server
                .jwt()
                .jwtAuthenticationConverter(jwtAuthenticationConverter)
                .and())
            .oauth2Client(withDefaults());
        return http.build();
    }

    @Bean
    public AuthorizedRolesProvider authorizedRolesProvider() {
        return new SpringAuthorizedRolesProvider();
    }

    @Bean
    public CcdEventAuthorizor getCcdEventAuthorizor(AuthorizedRolesProvider authorizedRolesProvider) {

        return new CcdEventAuthorizor(
            ImmutableMap.copyOf(roleEventAccess),
            authorizedRolesProvider
        );
    }

    public Map<String, List<Event>> getRoleEventAccess() {
        return roleEventAccess;
    }
}
