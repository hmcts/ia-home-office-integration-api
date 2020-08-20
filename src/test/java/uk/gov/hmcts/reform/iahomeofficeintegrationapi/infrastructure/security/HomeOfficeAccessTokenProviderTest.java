package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security.homeoffice.HomeOfficeAuthorizor;


@ExtendWith(MockitoExtension.class)
class HomeOfficeAccessTokenProviderTest {

    @Mock
    private HomeOfficeAuthorizor homeOfficeAuthorizor;

    private HomeOfficeAccessTokenProvider homeOfficeAccessTokenProvider;

    @BeforeEach
    public void setUp() {

        homeOfficeAccessTokenProvider = new HomeOfficeAccessTokenProvider(homeOfficeAuthorizor);
    }


    @Test
    void get_access_token_from_idam() {

        String expectedAccessToken = "access-token";

        when(homeOfficeAuthorizor.fetchCodeAuthorization()).thenReturn(expectedAccessToken);

        String actualAccessToken = homeOfficeAccessTokenProvider.getAccessToken();

        assertEquals(expectedAccessToken, actualAccessToken);
    }

    @Test
    void get_missing_access_token_from_idam_throws_if_not_a_try_attempt() {

        when(homeOfficeAuthorizor.fetchCodeAuthorization()).thenReturn(null);

        assertThatThrownBy(() -> homeOfficeAccessTokenProvider.getAccessToken())
            .hasMessage("System access token not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void try_get_access_token_from_idam() {

        String expectedAccessToken = "access-token";

        when(homeOfficeAuthorizor.fetchCodeAuthorization()).thenReturn(expectedAccessToken);

        Optional<String> optionalAccessToken = homeOfficeAccessTokenProvider.tryGetAccessToken();

        assertTrue(optionalAccessToken.isPresent());
        assertEquals(expectedAccessToken, optionalAccessToken.get());
    }

    @Test
    void try_get_access_token_from_idam_when_it_returns_null() {

        when(homeOfficeAuthorizor.fetchCodeAuthorization()).thenReturn(null);

        Optional<String> optionalAccessToken = homeOfficeAccessTokenProvider.tryGetAccessToken();

        assertFalse(optionalAccessToken.isPresent());
    }
}
