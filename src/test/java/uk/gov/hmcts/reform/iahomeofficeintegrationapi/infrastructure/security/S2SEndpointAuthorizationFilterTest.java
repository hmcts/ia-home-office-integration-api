package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class S2SEndpointAuthorizationFilterTest {

    @Mock
    private AuthTokenValidator authTokenValidator;

    private S2SEndpointAuthorizationFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new S2SEndpointAuthorizationFilter(authTokenValidator);
        
        // Set IAC configuration values - matching application.yaml
        ReflectionTestUtils.setField(filter, "iacAllowedEndpoints", 
            List.of("/asylum/ccdAboutToStart", "/asylum/ccdAboutToSubmit"));
        ReflectionTestUtils.setField(filter, "iacServices", 
            List.of("ccd", "ccd_data", "ccd_gw", "ccd_ps", "iac"));
        
        // Set Home Office configuration values
        ReflectionTestUtils.setField(filter, "homeOfficeAllowedEndpoints", 
            List.of("/home-office-statutory-timeframe-status"));
        ReflectionTestUtils.setField(filter, "homeOfficeServices", 
            List.of("home-office-immigration"));
        
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
    }

    @Test
    void should_allow_request_when_no_s2s_token_present() throws ServletException, IOException {
        // Given
        request.setRequestURI("/some-endpoint");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
        verify(authTokenValidator, never()).getServiceName(anyString());
    }

    @Test
    void should_allow_home_office_service_to_access_allowed_endpoint() throws ServletException, IOException {
        // Given
        String token = "test-token";
        request.setRequestURI("/home-office-statutory-timeframe-status");
        request.addHeader("ServiceAuthorization", token);
        when(authTokenValidator.getServiceName("Bearer " + token)).thenReturn("home-office-immigration");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
        verify(authTokenValidator).getServiceName("Bearer " + token);
    }

    @Test
    void should_deny_home_office_service_access_to_unauthorized_endpoint() throws ServletException, IOException {
        // Given
        String token = "test-token";
        request.setRequestURI("/s2stoken");
        request.addHeader("ServiceAuthorization", token);
        when(authTokenValidator.getServiceName("Bearer " + token)).thenReturn("home-office-immigration");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
        verify(authTokenValidator).getServiceName("Bearer " + token);
    }

    @Test
    void should_handle_token_with_bearer_prefix() throws ServletException, IOException {
        // Given
        String token = "Bearer test-token";
        request.setRequestURI("/home-office-statutory-timeframe-status");
        request.addHeader("ServiceAuthorization", token);
        when(authTokenValidator.getServiceName(token)).thenReturn("home-office-immigration");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
        verify(authTokenValidator).getServiceName(token);
    }

    @Test
    void should_deny_home_office_access_to_serviceusertoken_endpoint() throws ServletException, IOException {
        // Given
        String token = "test-token";
        request.setRequestURI("/serviceusertoken");
        request.addHeader("ServiceAuthorization", token);
        when(authTokenValidator.getServiceName("Bearer " + token)).thenReturn("home-office-immigration");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
        verify(authTokenValidator).getServiceName("Bearer " + token);
    }

    @Test
    void should_allow_iac_service_to_access_allowed_endpoints() throws ServletException, IOException {
        // Given
        String token = "test-token";
        request.setRequestURI("/asylum/ccdAboutToStart");
        request.addHeader("ServiceAuthorization", token);
        when(authTokenValidator.getServiceName("Bearer " + token)).thenReturn("iac");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
        verify(authTokenValidator).getServiceName("Bearer " + token);
    }

    @Test
    void should_deny_iac_service_access_to_unauthorized_endpoint() throws ServletException, IOException {
        // Given
        String token = "test-token";
        request.setRequestURI("/s2stoken");
        request.addHeader("ServiceAuthorization", token);
        when(authTokenValidator.getServiceName("Bearer " + token)).thenReturn("iac");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
        verify(authTokenValidator).getServiceName("Bearer " + token);
    }

    @Test
    void should_allow_ccd_service_to_access_allowed_endpoints() throws ServletException, IOException {
        // Given
        String token = "test-token";
        request.setRequestURI("/asylum/ccdAboutToSubmit");
        request.addHeader("ServiceAuthorization", token);
        when(authTokenValidator.getServiceName("Bearer " + token)).thenReturn("ccd");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    }

    @Test
    void should_allow_ccd_data_service_to_access_allowed_endpoints() throws ServletException, IOException {
        // Given
        String token = "test-token";
        request.setRequestURI("/asylum/ccdAboutToStart");
        request.addHeader("ServiceAuthorization", token);
        when(authTokenValidator.getServiceName("Bearer " + token)).thenReturn("ccd_data");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    }

    @Test
    void should_allow_service_in_multiple_groups_to_access_endpoints_from_any_group() throws ServletException, IOException {
        // Given - iac service is in both iacServices and homeOfficeServices
        ReflectionTestUtils.setField(filter, "homeOfficeServices",
            List.of("home-office-immigration", "iac"));

        String token = "test-token";
        request.setRequestURI("/home-office-statutory-timeframe-status");
        request.addHeader("ServiceAuthorization", token);
        when(authTokenValidator.getServiceName("Bearer " + token)).thenReturn("iac");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then - iac can access home-office endpoint because it's in homeOfficeServices
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    }

    @Test
    void should_allow_service_in_multiple_groups_to_access_iac_endpoints() throws ServletException, IOException {
        // Given - iac service is in both iacServices and homeOfficeServices
        ReflectionTestUtils.setField(filter, "homeOfficeServices",
            List.of("home-office-immigration", "iac"));

        String token = "test-token";
        request.setRequestURI("/asylum/ccdAboutToStart");
        request.addHeader("ServiceAuthorization", token);
        when(authTokenValidator.getServiceName("Bearer " + token)).thenReturn("iac");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then - iac can still access IAC endpoints
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    }

    @Test
    void should_deny_unknown_service() throws ServletException, IOException {
        // Given
        String token = "test-token";
        request.setRequestURI("/asylum/ccdAboutToStart");
        request.addHeader("ServiceAuthorization", token);
        when(authTokenValidator.getServiceName("Bearer " + token)).thenReturn("unknown-service");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
    }
}
