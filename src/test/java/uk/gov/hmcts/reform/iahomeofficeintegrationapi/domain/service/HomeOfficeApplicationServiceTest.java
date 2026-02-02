package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;

import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeApplicationDto;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.HomeOfficeApplicationApi;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.HomeOfficeMissingApplicationException;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.config.HomeOfficeProperties;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security.AccessTokenProvider;

class HomeOfficeApplicationServiceTest {

    @Mock
    private HomeOfficeProperties homeOfficeProperties;

    @Mock
    private HomeOfficeApplicationApi homeOfficeApplicationApi;

    @Mock
    private AccessTokenProvider accessTokenProvider;

    private HomeOfficeApplicationService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create a Map<String, LookupReferenceData> as expected by HomeOfficeProperties
        Map<String, HomeOfficeProperties.LookupReferenceData> codesMap = new HashMap<>();
        HomeOfficeProperties.LookupReferenceData consumerCode = mock(HomeOfficeProperties.LookupReferenceData.class);
        when(consumerCode.getCode()).thenReturn("CONSUMER123");
        codesMap.put("consumer", consumerCode);

        when(homeOfficeProperties.getCodes()).thenReturn(codesMap);

        service = new HomeOfficeApplicationService(
            homeOfficeProperties,
            homeOfficeApplicationApi,
            accessTokenProvider
        );
    }

    @Test
    void getApplication_shouldReturnDto_whenApiRespondsSuccessfully() {
        final String referenceNumber = "REF123";
        final String token = "ACCESS_TOKEN";
        HomeOfficeApplicationDto dto = new HomeOfficeApplicationDto();
        dto.setHoClaimDate(java.time.LocalDate.of(2026, 2, 2));

        when(accessTokenProvider.getAccessToken()).thenReturn(token);
        when(homeOfficeApplicationApi.getApplication(anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn(ResponseEntity.ok(dto));

        HomeOfficeApplicationDto result = service.getApplication(referenceNumber);

        assertThat(result).isNotNull();
        assertThat(result.getHoClaimDate()).isEqualTo(dto.getHoClaimDate());

        verify(accessTokenProvider).getAccessToken();

        ArgumentCaptor<String> referenceCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> correlationCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> consumerCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> eventDateCaptor = ArgumentCaptor.forClass(String.class);

        verify(homeOfficeApplicationApi).getApplication(
            referenceCaptor.capture(),
            eq(token),
            correlationCaptor.capture(),
            consumerCaptor.capture(),
            eventDateCaptor.capture()
        );

        assertThat(referenceCaptor.getValue()).isEqualTo(referenceNumber);
        assertThat(consumerCaptor.getValue()).isEqualTo("CONSUMER123");
        assertThat(correlationCaptor.getValue()).isNotBlank();
        assertThat(eventDateCaptor.getValue()).isNotBlank();
    }

    @Test
    void getApplication_shouldThrowHomeOfficeMissingApplicationException_for400() {
        assertFeignStatusThrows(400, "REF400", "not correctly formed");
    }

    @Test
    void getApplication_shouldThrowHomeOfficeMissingApplicationException_for401() {
        assertFeignStatusThrows(401, "REF401", "could not be authenticated");
    }

    @Test
    void getApplication_shouldThrowHomeOfficeMissingApplicationException_for403() {
        assertFeignStatusThrows(403, "REF403", "authenticated but not authorised");
    }

    @Test
    void getApplication_shouldThrowHomeOfficeMissingApplicationException_for404() {
        assertFeignStatusThrows(404, "REF404", "No application matching this HMCTS reference number was found");
    }

    @Test
    void getApplication_shouldThrowHomeOfficeMissingApplicationException_for500() {
        assertFeignStatusThrows(500, "REF500", "was not available");
    }

    @Test
    void getApplication_shouldThrowHomeOfficeMissingApplicationException_for501() {
        assertFeignStatusThrows(501, "REF501", "was not available");
    }

    @Test
    void getApplication_shouldThrowHomeOfficeMissingApplicationException_for502() {
        assertFeignStatusThrows(502, "REF502", "was not available");
    }

    @Test
    void getApplication_shouldThrowHomeOfficeMissingApplicationException_for503() {
        assertFeignStatusThrows(503, "REF503", "was not available");
    }

    @Test
    void getApplication_shouldThrowHomeOfficeMissingApplicationException_for504() {
        assertFeignStatusThrows(504, "REF504", "was not available");
    }

    // Helper method to reduce duplication for FeignException testing
    private void assertFeignStatusThrows(int status, String referenceNumber, String expectedMessageFragment) {
        FeignException feignEx = FeignException.errorStatus(
            "getApplication",
            feign.Response.builder()
                .status(status)
                .request(Request.create(Request.HttpMethod.GET, "", Map.of(), null, StandardCharsets.UTF_8, new RequestTemplate()))
                .build()
        );

        when(accessTokenProvider.getAccessToken()).thenReturn("token");
        when(homeOfficeApplicationApi.getApplication(anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenThrow(feignEx);

        assertThatThrownBy(() -> service.getApplication(referenceNumber))
            .isInstanceOf(HomeOfficeMissingApplicationException.class)
            .hasMessageContaining(expectedMessageFragment)
            .hasMessageContaining(referenceNumber);
    }
}
