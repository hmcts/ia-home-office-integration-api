package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S2STokenValidatorTest {

    @Mock
    private AuthTokenValidator authTokenValidator;

    private S2STokenValidator s2STokenValidator;

    @BeforeEach
    void setUp() {
        s2STokenValidator = new S2STokenValidator(authTokenValidator);
    }

    @Test
    void should_pass_validation_with_custom_allowed_services() {
        // Given
        String token = "test-token";
        String serviceName = "custom_service";
        List<String> allowedServices = Arrays.asList("custom_service", "another_service");
        when(authTokenValidator.getServiceName("Bearer " + token)).thenReturn(serviceName);

        // When & Then
        assertDoesNotThrow(() -> s2STokenValidator.checkIfServiceIsAllowed(token, allowedServices));
        verify(authTokenValidator).getServiceName("Bearer " + token);
    }

    @Test
    void should_throw_exception_when_service_not_in_custom_allowed_list() {
        // Given
        String token = "test-token";
        String serviceName = "ia";
        List<String> allowedServices = Arrays.asList("custom_service", "another_service");
        when(authTokenValidator.getServiceName("Bearer " + token)).thenReturn(serviceName);

        // When & Then
        assertThatThrownBy(() -> s2STokenValidator.checkIfServiceIsAllowed(token, allowedServices))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessage("Service 'ia' is not authorised to access this endpoint.");
    }

    @Test
    void should_throw_exception_when_service_name_is_null_with_custom_allowed_services() {
        // Given
        String token = "test-token";
        List<String> allowedServices = Arrays.asList("custom_service");
        when(authTokenValidator.getServiceName(anyString())).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> s2STokenValidator.checkIfServiceIsAllowed(token, allowedServices))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessage("Service name from S2S token ('ServiceAuthorization' header) is null");
    }
}
