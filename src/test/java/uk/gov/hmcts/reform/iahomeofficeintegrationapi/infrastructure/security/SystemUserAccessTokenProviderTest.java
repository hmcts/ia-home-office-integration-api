package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security.idam.IdamAuthorizor;

@ExtendWith(MockitoExtension.class)
public class SystemUserAccessTokenProviderTest {

    private static final String SYSTEM_USERNAME = "system";
    private static final String SYSTEM_PASSWORD = "secret";

    @Mock
    private IdamAuthorizor idamAuthorizor;

    private SystemUserAccessTokenProvider systemUserAccessTokenProvider;

    @BeforeEach
    public void setUp() {

        systemUserAccessTokenProvider =
            new SystemUserAccessTokenProvider(
                SYSTEM_USERNAME,
                SYSTEM_PASSWORD,
                idamAuthorizor
            );
    }

    @Test
    public void get_access_token_from_idam() {

        String expectedAccessToken = "access-token";

        when(idamAuthorizor.exchangeForAccessToken(SYSTEM_USERNAME, SYSTEM_PASSWORD)).thenReturn(expectedAccessToken);

        String actualAccessToken = systemUserAccessTokenProvider.getAccessToken();

        assertEquals(expectedAccessToken, actualAccessToken);
    }

    @Test
    public void get_missing_access_token_from_idam_throws_if_not_a_try_attempt() {

        when(idamAuthorizor.exchangeForAccessToken(SYSTEM_USERNAME, SYSTEM_PASSWORD)).thenReturn(null);

        assertThatThrownBy(() -> systemUserAccessTokenProvider.getAccessToken())
            .hasMessage("System access token not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void try_get_access_token_from_idam() {

        String expectedAccessToken = "access-token";

        when(idamAuthorizor.exchangeForAccessToken(SYSTEM_USERNAME, SYSTEM_PASSWORD)).thenReturn(expectedAccessToken);

        Optional<String> optionalAccessToken = systemUserAccessTokenProvider.tryGetAccessToken();

        assertTrue(optionalAccessToken.isPresent());
        assertEquals(expectedAccessToken, optionalAccessToken.get());
    }

    @Test
    public void try_get_access_token_from_idam_when_it_returns_null() {

        when(idamAuthorizor.exchangeForAccessToken(SYSTEM_USERNAME, SYSTEM_PASSWORD)).thenReturn(null);

        Optional<String> optionalAccessToken = systemUserAccessTokenProvider.tryGetAccessToken();

        assertFalse(optionalAccessToken.isPresent());
    }
}
