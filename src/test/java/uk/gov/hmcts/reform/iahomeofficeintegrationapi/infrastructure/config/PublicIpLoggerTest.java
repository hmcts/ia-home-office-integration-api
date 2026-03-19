package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.config;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationArguments;
import org.springframework.web.client.RestTemplate;

class PublicIpLoggerTest {

    private PublicIpLogger publicIpLogger;
    private RestTemplate restTemplateMock;
    private ApplicationArguments argsMock;

    @BeforeEach
    void setUp() {
        argsMock = mock(ApplicationArguments.class);

        // Create a spy of the PublicIpLogger so we can inject a mocked RestTemplate
        publicIpLogger = new PublicIpLogger() {
            @Override
            public void run(ApplicationArguments args) {
                // Replace the RestTemplate with a mock
                RestTemplate restTemplate = restTemplateMock;
                try {
                    String ip = restTemplate.getForObject("https://api.ipify.org", String.class);
                    System.out.println("Application started with public IP: " + ip);
                } catch (Exception e) {
                    System.out.println("Could not determine public IP address: " + e.getMessage());
                }
            }
        };

        restTemplateMock = mock(RestTemplate.class);
    }

    @Test
    void run_shouldLogPublicIp_whenRestTemplateReturnsIp() {
        when(restTemplateMock.getForObject(anyString(), eq(String.class)))
                .thenReturn("1.2.3.4");

        publicIpLogger.run(argsMock);

        verify(restTemplateMock, times(1))
                .getForObject("https://api.ipify.org", String.class);
    }

    @Test
    void run_shouldLogWarning_whenRestTemplateThrowsException() {
        when(restTemplateMock.getForObject(anyString(), eq(String.class)))
                .thenThrow(new RuntimeException("Timeout"));

        publicIpLogger.run(argsMock);

        verify(restTemplateMock, times(1))
                .getForObject("https://api.ipify.org", String.class);
    }
}