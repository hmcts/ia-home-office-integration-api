package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.config.HomeOfficeProperties.LookupReferenceData;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.util.FileCopyUtils;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeSearch;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeSearchResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.HomeOfficeSearchApi;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.config.HomeOfficeProperties;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security.AccessTokenProvider;

@SpringJUnitConfig
@ExtendWith(MockitoExtension.class)
class HomeOfficeSearchServiceTest {

    private final String someHoReference = "some-ho-reference";
    @Value("classpath:home-office-sample-response.json")
    private Resource resource;
    @Mock
    private HomeOfficeProperties homeOfficeProperties;
    @Mock
    private HomeOfficeSearchApi homeOfficeSearchApi;
    @Mock
    private @Qualifier("requestUser") AccessTokenProvider accessTokenProvider;

    private HomeOfficeSearchService homeOfficeSearchService;

    @BeforeEach
    public void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        homeOfficeSearchService = new HomeOfficeSearchService(
            homeOfficeProperties, homeOfficeSearchApi, accessTokenProvider, objectMapper);
    }

    @Test
    void should_return_case_details_from_home_office() throws Exception {

        when(homeOfficeProperties.getCodes()).thenReturn(getHomeOfficeReferenceData());
        when(homeOfficeSearchApi.getStatus(any(), any())).thenReturn(
            getSampleResponse()
        );

        HomeOfficeSearchResponse response = homeOfficeSearchService.getCaseStatus(someHoReference);

        assertNotNull(response);
        assertNotNull(response.getMessageHeader());
        assertNotNull(response.getMessageType());
        assertThat(response.getMessageType()).isEqualTo("RESPONSE_RIGHT_OF_APPEAL_DETAILS");
        assertNotNull(response.getStatus().get(0).getPerson());
        assertNotNull(response.getStatus().get(0).getApplicationStatus());
        assertNotNull(response.getStatus().get(0).getPerson().getGivenName());
        assertNotNull(response.getStatus().get(0).getPerson().getFamilyName());
        assertNotNull(response.getStatus().get(0).getPerson().getFullName());
        assertNotNull(response.getStatus().get(0).getPerson().getNationality());
        assertNotNull(response.getStatus().get(0).getApplicationStatus().getDecisionDate());
        assertNotNull(response.getStatus().get(0).getApplicationStatus().getDecisionType());
    }

    @Test
    void returns_values_in_request_body() {

        doReturn(getHomeOfficeReferenceData()).when(homeOfficeProperties).getCodes();

        HomeOfficeSearch request = homeOfficeSearchService.makeRequestBody(someHoReference);

        assertNotNull(request);
        assertEquals("DOCUMENT_REFERENCE", request.getSearchParams().get(0).getSpType());
        assertEquals(someHoReference, request.getSearchParams().get(0).getSpValue());
        assertEquals("HMCTS", request.getMessageHeader().getConsumer().getCode());
        assertEquals("HM Courts and Tribunal Service",
            request.getMessageHeader().getConsumer().getDescription());
        assertNotNull(request.getMessageHeader().getEventDateTime());
        assertNotNull(request.getMessageHeader().getCorrelationId());
    }

    @Test
    void should_throw_for_null_home_office_reference() {

        assertThatThrownBy(() -> homeOfficeSearchService.getCaseStatus(null))
            .isExactlyInstanceOf(NullPointerException.class);
    }

    private Map<String, LookupReferenceData> getHomeOfficeReferenceData() {
        final Map<String, LookupReferenceData> consumerMap = new HashMap<>();
        LookupReferenceData lookupReferenceData = new LookupReferenceData();
        lookupReferenceData.setCode("HMCTS");
        lookupReferenceData.setDescription("HM Courts and Tribunal Service");
        consumerMap.put("consumer", lookupReferenceData);
        return consumerMap;
    }

    private String getSampleResponse() throws Exception {
        Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8);
        return FileCopyUtils.copyToString(reader);
        //ObjectMapper om = new ObjectMapper()
        //    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //return om.readValue(FileCopyUtils.copyToString(reader), HomeOfficeSearchResponse.class);
    }

}
