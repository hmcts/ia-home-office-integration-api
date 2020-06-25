package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.util.HomeOfficeMessageHeaderCreator;

@ExtendWith(MockitoExtension.class)
public class HomeOfficeCaseStatusClientTest {

    private final String someEndpoint = "some-endpoint";
    private final String someUri = "some-uri";
    private final String someResponse = "some-response";
    private final String someHomeOfficeReference = "some-ho-reference";

    @Mock
    private RestTemplate restTemplate;
    @Mock
    private HttpClientErrorException httpClientErrorException;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private HomeOfficeMessageHeaderCreator homeOfficeMessageHeaderCreator;
    @Mock
    private ObjectMapper objectMapper;
    @Captor
    private ArgumentCaptor<HttpEntity<Map<String, List>>> httpEntityArgumentCaptor;
    private HomeOfficeCaseStatusClient homeOfficeCaseStatusClient;

    @BeforeEach
    public void setUp() throws IOException {

        homeOfficeCaseStatusClient = new HomeOfficeCaseStatusClient(
            someEndpoint,
            someUri,
            restTemplate,
            homeOfficeMessageHeaderCreator);
    }

    @Test
    public void returns_case_data_from_home_office_api_call_for_sample_response() {

        when(asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER))
            .thenReturn(Optional.of(someHomeOfficeReference));

        doReturn(someResponse).when(restTemplate).postForObject(
            anyString(),
            any(HttpEntity.class),
            eq(String.class));

        String actualResponse = homeOfficeCaseStatusClient.getCaseStatus(asylumCase);

        verify(restTemplate, times(1))
            .postForObject(
                eq(someEndpoint + someUri),
                httpEntityArgumentCaptor.capture(),
                eq(String.class));

        assertThat(actualResponse).isEqualTo(someResponse);
    }

    @Test
    public void handles_http_exception() {

        when(asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER))
            .thenReturn(Optional.of(someHomeOfficeReference));

        when(restTemplate.postForObject(
            anyString(),
            any(HttpEntity.class),
            any())).thenThrow(httpClientErrorException);

        when(httpClientErrorException.getResponseBodyAsString())
            .thenReturn("some-response-body");

        assertThatThrownBy(() -> homeOfficeCaseStatusClient.getCaseStatus(asylumCase))
            .isExactlyInstanceOf(HomeOfficeResponseException.class)
            .hasMessage("some-response-body")
            .hasCause(httpClientErrorException);
    }

    @Test
    public void create_searchParams_should_return_home_office_reference_number() {
        when(asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER))
            .thenReturn(Optional.of(someHomeOfficeReference));

        Object[] searchParams = homeOfficeCaseStatusClient.createSearchParams(asylumCase);

        assertNotNull(searchParams);
        assertThat(searchParams.length).isEqualTo(1);
        assertThat(((Map) searchParams[0]).get("spType")).isEqualTo("DOCUMENT_REFERENCE");
        assertThat(((Map) searchParams[0]).get("spValue")).isEqualTo(someHomeOfficeReference);

    }

    @Test
    public void createRequest_should_return_valid_request_body() {
        when(asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER))
            .thenReturn(Optional.of(someHomeOfficeReference));

        HttpEntity<Map<String, Object>> httpEntity = homeOfficeCaseStatusClient.createRequest(asylumCase);

        assertNotNull(httpEntity);
        assertThat(httpEntity.getBody().size()).isEqualTo(2);
        assertNotNull(httpEntity.getBody().get("messageHeader"));
        assertNotNull(httpEntity.getBody().get("searchParams"));
    }
}
