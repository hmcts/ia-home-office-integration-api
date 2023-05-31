package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security.homeoffice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Map;
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

    //private static final String JWT_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhY2Nlc3NfdG9rZW4iOiJzb21lX2FjY2V"
    //                                        + "zc190b2tlbiIsInNjb3BlIjoicmVhZCIsInRva2VuX3R5cGUiOiJiZWFyZXIiLCJleHBpc"
    //                                        + "mVzX2luIjoyOTl9.rsrnW2pMzmSJiY_80IxvgOTgglgLGBiYtAFRRuNVVqc";
    private static final String JWT_TOKEN = "{"
                                            + "\"access_token\": \"some_access_token\","
                                            + "\"expires_in\": 300,"
                                            + "\"token_type\": \"bearer\","
                                            + "\"not-before-policy\": 0,"
                                            + "\"scope\": \"email profile\""
                                            + "}";


    @Mock HomeOfficeTokenApi homeOfficeTokenApi;

    @Test
    void should_call_homeoffice_api_to_authorize() {
        HomeOfficeAuthorizor homeOfficeAuthorizor = new HomeOfficeAuthorizor(homeOfficeTokenApi, BASE_URL,
            TOKEN_PATH, CLIENT_ID, CLIENT_SECRET);

        doReturn(JWT_TOKEN)
            .when(homeOfficeTokenApi)
            .getAuthorizationToken(
                anyMap()
            );

        String actualAccessToken = homeOfficeAuthorizor.fetchCodeAuthorization();

        assertEquals("Bearer some_access_token", actualAccessToken);

        ArgumentCaptor<Map<String, ?>> requestCaptor = ArgumentCaptor.forClass(Map.class);

        verify(homeOfficeTokenApi, times(1)).getAuthorizationToken(
            requestCaptor.capture()
        );

        final Map<String, ?> actualTokenParameters = requestCaptor.getValue();

        assertEquals("client_credentials", actualTokenParameters.get("grant_type"));
        assertEquals(CLIENT_ID, actualTokenParameters.get("client_id"));
        assertEquals(CLIENT_SECRET, actualTokenParameters.get("client_secret"));
    }

    @Test
    void should_break_when_empty_clientid_secret() {
        assertThrows(IllegalArgumentException.class, () -> {
            new HomeOfficeAuthorizor(homeOfficeTokenApi, BASE_URL, TOKEN_PATH, "", "something");
        });

    }

    @Test
    void should_break_when_empty_clientsecret_secret() {
        assertThrows(IllegalArgumentException.class, () -> {
            new HomeOfficeAuthorizor(homeOfficeTokenApi, BASE_URL, TOKEN_PATH, "something", "");
        });

    }

    @Test
    void should_call_homeoffice_api_to_authorize_and_get_empty_token() {
        // Given
        HomeOfficeAuthorizor homeOfficeAuthorizor = new HomeOfficeAuthorizor(homeOfficeTokenApi, BASE_URL,
            TOKEN_PATH, CLIENT_ID, CLIENT_SECRET);

        doReturn(null).when(homeOfficeTokenApi).getAuthorizationToken(anyMap());

        // When
        // Then an exception will be thrown
        assertThrows(Exception.class, () -> {
            homeOfficeAuthorizor.fetchCodeAuthorization();
        });
    }

}
