package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeAppellantDto;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeApplicationDto;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.HomeOfficeApplicationApi;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.HomeOfficeMissingApplicationException;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.RetriesExceededException;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.config.HomeOfficeProperties;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security.AccessTokenProvider;

@ExtendWith(MockitoExtension.class)
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

        // Prepare HomeOfficeProperties with a "consumer" code
        HomeOfficeProperties.LookupReferenceData consumerLookup = new HomeOfficeProperties.LookupReferenceData();
        consumerLookup.setCode("CONSUMER123");

        Map<String, HomeOfficeProperties.LookupReferenceData> codesMap = new HashMap<>();
        codesMap.put("consumer", consumerLookup);

        when(homeOfficeProperties.getCodes()).thenReturn(codesMap);

        service = new HomeOfficeApplicationService(
            homeOfficeProperties,
            homeOfficeApplicationApi,
            accessTokenProvider
        );
    }

    @Test
    void getApplication_shouldReturnDto_whenApiRespondsSuccessfully() {
        // Arrange
        String referenceNumber = "REF123";
        String token = "ACCESS_TOKEN";
        HomeOfficeApplicationDto dto = new HomeOfficeApplicationDto();

        when(accessTokenProvider.getAccessToken()).thenReturn(token);

        when(homeOfficeApplicationApi.getApplication(
            anyString(),
            eq(token),
            anyString(),
            eq("CONSUMER123"),
            anyString()
        )).thenReturn(new ResponseEntity<>(dto, HttpStatus.OK));

        // Act
        ResponseEntity<HomeOfficeApplicationDto> result = service.getApplication(referenceNumber);
        HomeOfficeApplicationDto body = result.getBody();
        // Assert
        assertThat(body).isNotNull();
        assertThat(body).isSameAs(dto);

        // Verify interactions
        verify(accessTokenProvider).getAccessToken();
        verify(homeOfficeApplicationApi).getApplication(
            eq(referenceNumber),
            eq(token),
            anyString(),        // correlationId
            eq("CONSUMER123"),
            anyString()         // eventDateTime
        );
    }

    @Test
    void getApplication_shouldLogAppellants_whenApiReturnsNonEmptyAppellantList() throws HomeOfficeMissingApplicationException {
        // Arrange
        String referenceNumber = "1234-5678-9012-3456";
        final String token = "ACCESS_TOKEN";

        HomeOfficeAppellantDto appellantDto = new HomeOfficeAppellantDto();
        appellantDto.setFamilyName("Smith");
        appellantDto.setGivenNames("John");

        HomeOfficeApplicationDto dto = new HomeOfficeApplicationDto();
        dto.setUan(referenceNumber);
        dto.setAppellants(List.of(appellantDto));

        when(accessTokenProvider.getAccessToken()).thenReturn(token);
        when(homeOfficeApplicationApi.getApplication(
            anyString(), eq(token), anyString(), eq("CONSUMER123"), anyString()
        )).thenReturn(new ResponseEntity<>(dto, HttpStatus.OK));

        // Act
        ResponseEntity<HomeOfficeApplicationDto> result = service.getApplication(referenceNumber);

        // Assert
        assertThat(result.getBody()).isSameAs(dto);
        assertThat(result.getBody().getAppellants()).hasSize(1);
    }

    @Test
    void getApplication_shouldReturnNull_whenApiReturnsNullBody() {
        // Arrange
        String referenceNumber = "REF456";
        String token = "ACCESS_TOKEN";

        when(accessTokenProvider.getAccessToken()).thenReturn(token);

        when(homeOfficeApplicationApi.getApplication(
            anyString(),
            eq(token),
            anyString(),
            eq("CONSUMER123"),
            anyString()
        )).thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        // Act
        ResponseEntity<HomeOfficeApplicationDto> result = service.getApplication(referenceNumber);
        HomeOfficeApplicationDto body = result.getBody();

        // Assert
        assertThat(body).isNull();

        verify(accessTokenProvider).getAccessToken();
        verify(homeOfficeApplicationApi).getApplication(
            eq(referenceNumber),
            eq(token),
            anyString(),
            eq("CONSUMER123"),
            anyString()
        );
    }

    @Test
    void getApplication_shouldThrowHomeOfficeMissingApplicationException_whenRetriesExceeded() {
        // Arrange
        String referenceNumber = "REF789";
        String token = "ACCESS_TOKEN";

        when(accessTokenProvider.getAccessToken()).thenReturn(token);
        when(homeOfficeApplicationApi.getApplication(
            anyString(), eq(token), anyString(), eq("CONSUMER123"), anyString()
        )).thenThrow(new RetriesExceededException("Connection timed out"));

        // Act & Assert
        assertThatThrownBy(() -> service.getApplication(referenceNumber))
            .isExactlyInstanceOf(HomeOfficeMissingApplicationException.class)
            .hasMessageContaining("The Home Office validation API did not respond");
    }
}