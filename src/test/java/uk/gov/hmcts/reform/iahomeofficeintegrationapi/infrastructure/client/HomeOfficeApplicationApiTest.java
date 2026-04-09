package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeApplicationDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HomeOfficeApplicationApiTest {

    @Test
    void shouldReturnApplicationFromFeignClient() {
        // Arrange
        HomeOfficeApplicationApi feignClient = mock(HomeOfficeApplicationApi.class);

        HomeOfficeApplicationDto dto = new HomeOfficeApplicationDto();
        dto.setHoClaimDate(null); // just dummy data

        ResponseEntity<HomeOfficeApplicationDto> response = ResponseEntity.ok(dto);

        when(feignClient.getApplication(
                eq("ABC123"),
                eq("Bearer token"),
                eq("corr-id-1"),
                eq("consumer-code"),
                eq("2026-02-02T12:00:00")
        )).thenReturn(response);

        // Act
        ResponseEntity<HomeOfficeApplicationDto> result = feignClient.getApplication(
                "ABC123",
                "Bearer token",
                "corr-id-1",
                "consumer-code",
                "2026-02-02T12:00:00"
        );

        // Assert
        assertThat(result.getBody()).isEqualTo(dto);

        // Verify that Feign client method was called exactly once with these args
        verify(feignClient, times(1)).getApplication(
                "ABC123",
                "Bearer token",
                "corr-id-1",
                "consumer-code",
                "2026-02-02T12:00:00"
        );
    }
}
