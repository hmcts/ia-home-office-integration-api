package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security.AuthorizedRolesProvider;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security.CcdEventAuthorizor;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security.S2SEndpointAuthorizationFilter;

class SecurityConfigurationTest {

    private SecurityConfiguration securityConfiguration;

    private Converter<Jwt, Collection<GrantedAuthority>> idamAuthoritiesConverter;
    private S2SEndpointAuthorizationFilter s2SEndpointAuthorizationFilter;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        idamAuthoritiesConverter = mock(Converter.class);
        s2SEndpointAuthorizationFilter = mock(S2SEndpointAuthorizationFilter.class);

        securityConfiguration = new SecurityConfiguration(
            idamAuthoritiesConverter,
            s2SEndpointAuthorizationFilter
        );
    }

    @Test
    void shouldReturnAnonymousPathsList() {

        List<String> anonymousPaths = securityConfiguration.getAnonymousPaths();

        assertThat(anonymousPaths).isNotNull();
        assertThat(anonymousPaths).isEmpty();
    }

    @Test
    void shouldReturnRoleEventAccessMap() {

        Map<String, List<Event>> roleEventAccess = securityConfiguration.getRoleEventAccess();

        assertThat(roleEventAccess).isNotNull();
        assertThat(roleEventAccess).isEmpty();
    }

    @Test
    void shouldCreateAuthorizedRolesProviderBean() {

        AuthorizedRolesProvider provider = securityConfiguration.authorizedRolesProvider();

        assertThat(provider).isNotNull();
    }

    @Test
    void shouldCreateCcdEventAuthorizorBean() {

        AuthorizedRolesProvider provider = mock(AuthorizedRolesProvider.class);

        CcdEventAuthorizor authorizor = securityConfiguration.getCcdEventAuthorizor(provider);

        assertThat(authorizor).isNotNull();
    }

}