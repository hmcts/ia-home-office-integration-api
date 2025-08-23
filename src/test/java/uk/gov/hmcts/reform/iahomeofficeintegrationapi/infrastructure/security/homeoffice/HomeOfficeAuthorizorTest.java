package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security.homeoffice;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.HomeOfficeTokenApi;


@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class HomeOfficeAuthorizorTest {

    private static final String BASE_URL = "http://base.url";
    private static final String TOKEN_PATH = "/ichallenge/token";
    private static final String CLIENT_ID = "1234";
    private static final String CLIENT_SECRET = "badgers";
    private static final String JWT_TOKEN = "{"
                                            + "\"access_token\": \"some_access_token\","
                                            + "\"expires_in\": 300,"
                                            + "\"token_type\": \"bearer\","
                                            + "\"not-before-policy\": 0,"
                                            + "\"scope\": \"email profile\""
                                            + "}";

    @Mock HomeOfficeTokenApi homeOfficeTokenApi;
    HomeOfficeAuthorizor homeOfficeAuthorizor;

    @BeforeEach
    void setUp() {
        homeOfficeAuthorizor =
            new HomeOfficeAuthorizor(homeOfficeTokenApi, BASE_URL, TOKEN_PATH, CLIENT_ID, CLIENT_SECRET);
    }

    @Test
    void should_call_homeoffice_api_to_authorize() {
        doReturn(JWT_TOKEN)
            .when(homeOfficeTokenApi)
            .getAuthorizationToken(
                anyMap()
            );

        String actualAccessToken = homeOfficeAuthorizor.fetchCodeAuthorization();

        Assertions.assertEquals("Bearer some_access_token", actualAccessToken);

        ArgumentCaptor<Map<String, ?>> requestCaptor = ArgumentCaptor.forClass(Map.class);

        verify(homeOfficeTokenApi, times(1)).getAuthorizationToken(
            requestCaptor.capture()
        );

        final Map<String, ?> actualTokenParameters = requestCaptor.getValue();

        Assertions.assertEquals("client_credentials", actualTokenParameters.get("grant_type"));
        Assertions.assertEquals(CLIENT_ID, actualTokenParameters.get("client_id"));
        Assertions.assertEquals(CLIENT_SECRET, actualTokenParameters.get("client_secret"));
    }

    @Test
    void should_call_homeoffice_api_to_authorize_and_get_empty_token() {
        doReturn(null).when(homeOfficeTokenApi).getAuthorizationToken(anyMap());

        assertThrows(Exception.class, homeOfficeAuthorizor::fetchCodeAuthorization);
    }

}
