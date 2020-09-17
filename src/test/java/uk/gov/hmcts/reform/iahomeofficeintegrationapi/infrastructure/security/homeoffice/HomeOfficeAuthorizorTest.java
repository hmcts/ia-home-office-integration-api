package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security.homeoffice;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security.idam.IdentityManagerResponseException;

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


    @Mock RestTemplate restTemplate;

    private HomeOfficeAuthorizor homeOfficeAuthorizor;

    @BeforeEach
    public void setUp() {

        homeOfficeAuthorizor =
            new HomeOfficeAuthorizor(
                restTemplate,
                BASE_URL,
                TOKEN_PATH,
                CLIENT_ID,
                CLIENT_SECRET
            );
    }

    @Test
    void should_call_homeoffice_api_to_authorize() {

        doReturn(new ResponseEntity<>(JWT_TOKEN, HttpStatus.OK))
            .when(restTemplate)
            .exchange(
                eq(BASE_URL + TOKEN_PATH),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                ArgumentMatchers.any(ParameterizedTypeReference.class)
            );

        String actualAccessToken = homeOfficeAuthorizor.fetchCodeAuthorization();

        assertEquals("Bearer some_access_token", actualAccessToken);

        ArgumentCaptor<HttpEntity>  tokenHttpEntityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        verify(restTemplate, times(1)).exchange(
            eq(BASE_URL + TOKEN_PATH),
            eq(HttpMethod.POST),
            tokenHttpEntityCaptor.capture(),
            any(ParameterizedTypeReference.class)
        );

        HttpEntity tokenHttpEntity = tokenHttpEntityCaptor.getAllValues().get(0);

        HttpHeaders actualTokenHeaders = tokenHttpEntity.getHeaders();
        assertEquals(MediaType.APPLICATION_FORM_URLENCODED, actualTokenHeaders.getContentType());

        MultiValueMap actualTokenParameters = (MultiValueMap) tokenHttpEntity.getBody();
        assertEquals("client_credentials", actualTokenParameters.getFirst("grant_type"));
        assertEquals(CLIENT_ID, actualTokenParameters.getFirst("client_id"));
        assertEquals(CLIENT_SECRET, actualTokenParameters.getFirst("client_secret"));
    }

    @Test
    void wrap_client_exception_when_calling_oauth_authorize() {

        HttpClientErrorException underlyingException = mock(HttpClientErrorException.class);

        when(restTemplate
            .exchange(
                eq(BASE_URL + TOKEN_PATH),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
            )
        ).thenThrow(underlyingException);

        assertThatThrownBy(() -> homeOfficeAuthorizor.fetchCodeAuthorization())
            .isExactlyInstanceOf(IdentityManagerResponseException.class)
            .hasMessage("Could not get auth code from the Home Office");

    }

    @Test
    void wrap_server_exception_when_calling_oauth_authorize() {

        HttpServerErrorException underlyingException = mock(HttpServerErrorException.class);

        when(restTemplate
            .exchange(
                eq(BASE_URL + TOKEN_PATH),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
            )
        ).thenThrow(underlyingException);

        assertThatThrownBy(() -> homeOfficeAuthorizor.fetchCodeAuthorization())
            .isExactlyInstanceOf(IdentityManagerResponseException.class)
            .hasMessage("Could not get auth code from the Home Office");

    }
}
