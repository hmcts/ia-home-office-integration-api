package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.util.HomeOfficeApiUtil;

@ExtendWith(MockitoExtension.class)
public class HomeOfficeSendNotificationClientTest {

    private final String someEndpoint = "some-endpoint";
    private final String someUri = "some-uri";
    private final String someHomeOfficeReference = "some-ho-reference";
    private final int okResponse = 200;
    private final ResponseEntity someResponseEntity = new ResponseEntity(HttpStatus.OK);
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private HttpClientErrorException httpClientErrorException;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private HomeOfficeApiUtil homeOfficeApiUtil;
    @Captor
    private ArgumentCaptor<HttpEntity<Map<String, List>>> httpEntityArgumentCaptor;
    private HomeOfficeSendNotificationClient homeOfficeSendNotificationClient;

    @BeforeEach
    public void setUp() throws IOException {

        homeOfficeSendNotificationClient = new HomeOfficeSendNotificationClient(
            someEndpoint,
            someUri,
            restTemplate,
            homeOfficeApiUtil);
    }

    @Test
    public void returns_200_status_from_home_office_api_instruct_sample_notification() {

        when(asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER))
            .thenReturn(Optional.of(someHomeOfficeReference));

        doReturn(someResponseEntity).when(restTemplate).postForEntity(
            anyString(),
            any(HttpEntity.class),
            eq(String.class));

        int actualResponse = homeOfficeSendNotificationClient.sendNotification(asylumCase);

        verify(restTemplate, times(1))
            .postForEntity(
                eq(someEndpoint + someUri),
                httpEntityArgumentCaptor.capture(),
                eq(String.class));

        assertThat(actualResponse).isEqualTo(okResponse);
    }

    @Test
    public void handles_http_exception() {

        when(restTemplate.postForEntity(
            anyString(),
            any(HttpEntity.class),
            any())).thenThrow(httpClientErrorException);

        when(httpClientErrorException.getResponseBodyAsString())
            .thenReturn("some-response-body");

        assertThatThrownBy(() -> homeOfficeSendNotificationClient.sendNotification(asylumCase))
            .isExactlyInstanceOf(HomeOfficeResponseException.class)
            .hasMessage("some-response-body")
            .hasCause(httpClientErrorException);
    }

    @Test
    public void createRequest_should_return_valid_request_body() {
        when(asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER))
            .thenReturn(Optional.of(someHomeOfficeReference));

        HttpEntity<Map<String, Object>> httpEntity = homeOfficeSendNotificationClient.createRequest(asylumCase);

        assertNotNull(httpEntity);
        assertThat(httpEntity.getBody().size()).isEqualTo(5);
        assertNotNull(httpEntity.getBody().get("consumerReference"));
        assertNotNull(httpEntity.getBody().get("courtOutcome"));
        assertNotNull(httpEntity.getBody().get("hoReference"));
        assertEquals(httpEntity.getBody().get("hoReference"),someHomeOfficeReference);
        assertNotNull(httpEntity.getBody().get("messageHeader"));
        assertEquals(httpEntity.getBody().get("messageType"),"REQUEST_CHALLENGE_END");
    }

}
