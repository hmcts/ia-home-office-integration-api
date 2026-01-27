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

        // Set anonymous paths - these bypass S2S validation entirely
        ReflectionTestUtils.setField(filter, "anonymousPaths",
            List.of("/health", "/"));

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
    }

    // ===========================================
    // Authentication failures (401) - S2S token issues
    // ===========================================

    @Test
    void should_return_401_when_no_s2s_token_present() throws ServletException, IOException {
        // Given
        request.setRequestURI("/some-endpoint");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(response.getContentAsString()).contains("Missing ServiceAuthorization header");
        verify(authTokenValidator, never()).getServiceName(anyString());
    }

    @Test
    void should_return_401_when_s2s_token_is_empty() throws ServletException, IOException {
        // Given
        request.setRequestURI("/some-endpoint");
        request.addHeader("ServiceAuthorization", "");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(response.getContentAsString()).contains("Missing ServiceAuthorization header");
    }

    @Test
    void should_return_401_when_s2s_token_is_invalid() throws ServletException, IOException {
        // Given
        String token = "invalid-token";
        request.setRequestURI("/some-endpoint");
        request.addHeader("ServiceAuthorization", token);
        when(authTokenValidator.getServiceName("Bearer " + token))
            .thenThrow(new RuntimeException("Token validation failed"));

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(response.getContentAsString()).contains("Invalid or expired S2S token");
    }

    @Test
    void should_return_401_when_s2s_token_is_expired() throws ServletException, IOException {
        // Given
        String token = "expired-token";
        request.setRequestURI("/some-endpoint");
        request.addHeader("ServiceAuthorization", token);
        when(authTokenValidator.getServiceName("Bearer " + token))
            .thenThrow(new RuntimeException("Token expired"));

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(response.getContentAsString()).contains("Invalid or expired S2S token");
    }

    @Test
    void should_return_401_for_unknown_service() throws ServletException, IOException {
        // Given
        String token = "test-token";
        request.setRequestURI("/asylum/ccdAboutToStart");
        request.addHeader("ServiceAuthorization", token);
        when(authTokenValidator.getServiceName("Bearer " + token)).thenReturn("unknown-service");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(response.getContentAsString()).contains("unknown-service");
        assertThat(response.getContentAsString()).contains("not a recognised service");
    }

    // ===========================================
    // Authorization failures (403) - endpoint access denied
    // ===========================================

    @Test
    void should_return_403_when_home_office_service_accesses_unauthorized_endpoint() throws ServletException, IOException {
        // Given
        String token = "test-token";
        request.setRequestURI("/s2stoken");
        request.addHeader("ServiceAuthorization", token);
        when(authTokenValidator.getServiceName("Bearer " + token)).thenReturn("home-office-immigration");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
        assertThat(response.getContentAsString()).contains("home-office-immigration");
        assertThat(response.getContentAsString()).contains("not authorised to access endpoint");
        verify(authTokenValidator).getServiceName("Bearer " + token);
    }

    @Test
    void should_return_403_when_iac_service_accesses_unauthorized_endpoint() throws ServletException, IOException {
        // Given
        String token = "test-token";
        request.setRequestURI("/home-office-statutory-timeframe-status");
        request.addHeader("ServiceAuthorization", token);
        when(authTokenValidator.getServiceName("Bearer " + token)).thenReturn("iac");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
        assertThat(response.getContentAsString()).contains("iac");
        assertThat(response.getContentAsString()).contains("not authorised to access endpoint");
    }

    @Test
    void should_return_403_when_home_office_accesses_serviceusertoken_endpoint() throws ServletException, IOException {
        // Given
        String token = "test-token";
        request.setRequestURI("/serviceusertoken");
        request.addHeader("ServiceAuthorization", token);
        when(authTokenValidator.getServiceName("Bearer " + token)).thenReturn("home-office-immigration");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
        assertThat(response.getContentAsString()).contains("not authorised to access endpoint");
    }

    // ===========================================
    // Successful access
    // ===========================================

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

    // ===========================================
    // Service in multiple groups
    // ===========================================

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

    // ===========================================
    // Anonymous paths - bypass S2S validation
    // ===========================================

    @Test
    void should_skip_s2s_validation_for_anonymous_paths() throws ServletException, IOException {
        // Given - anonymous path with invalid S2S token
        String invalidToken = "invalid-token";
        request.setRequestURI("/health");
        request.addHeader("ServiceAuthorization", invalidToken);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then - should pass without validating the token
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
        verify(authTokenValidator, never()).getServiceName(anyString());
    }

    @Test
    void should_skip_s2s_validation_for_anonymous_path_subpath() throws ServletException, IOException {
        // Given - subpath of anonymous path
        String invalidToken = "invalid-token";
        request.setRequestURI("/health/liveness");
        request.addHeader("ServiceAuthorization", invalidToken);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then - should pass without validating the token
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
        verify(authTokenValidator, never()).getServiceName(anyString());
    }

    @Test
    void should_skip_s2s_validation_for_anonymous_path_without_token() throws ServletException, IOException {
        // Given - anonymous path with no S2S token
        request.setRequestURI("/health");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then - should pass without requiring token
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
        verify(authTokenValidator, never()).getServiceName(anyString());
    }
}
