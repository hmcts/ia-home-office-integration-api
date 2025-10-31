package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S2STokenValidatorTest {

    private static final List<String> IA_S2S_AUTH_SERVICES = List.of("iac", "home_office_api");

    @Mock
    private AuthTokenValidator authTokenValidator;

    private S2STokenValidator s2STokenValidator;

    @BeforeEach
    public void setup() {
        s2STokenValidator = new S2STokenValidator(IA_S2S_AUTH_SERVICES, authTokenValidator);
    }

    @Test
    void givenServiceNameIsValid() {
        when(authTokenValidator.getServiceName("Bearer home_office_api")).thenReturn("home_office_api");
        s2STokenValidator.checkIfServiceIsAllowed("home_office_api");
        verify(authTokenValidator).getServiceName("Bearer home_office_api");
    }

    @Test
    void givenServiceNameIsNullFromToken() {
        when(authTokenValidator.getServiceName("Bearer TestService")).thenReturn(null);
        assertThrows(AccessDeniedException.class, () -> s2STokenValidator.checkIfServiceIsAllowed("TestService"));
    }

    @Test
    void givenServiceNameCouldNotBeFound() {
        when(authTokenValidator.getServiceName("Bearer TestService")).thenReturn("SERVICE_NAME_SHOULD_NOT_BE_FOUND");
        assertThrows(AccessDeniedException.class, () -> s2STokenValidator.checkIfServiceIsAllowed("TestService"));
    }

    @Test
    void givenServiceNameIsEmptyFromToken() {
        assertThrows(AccessDeniedException.class, () -> s2STokenValidator.checkIfServiceIsAllowed(""));
    }

    @Test
    void givenTokenDoesNotStartWithBearer() {
        String tokenWithoutBearer = "home_office_api";
        when(authTokenValidator.getServiceName("Bearer " + tokenWithoutBearer)).thenReturn(tokenWithoutBearer);
        s2STokenValidator.checkIfServiceIsAllowed(tokenWithoutBearer);
        verify(authTokenValidator).getServiceName("Bearer " + tokenWithoutBearer);
    }
}
