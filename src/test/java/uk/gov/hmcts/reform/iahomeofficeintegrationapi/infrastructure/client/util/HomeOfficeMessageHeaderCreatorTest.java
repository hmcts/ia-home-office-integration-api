package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig
@ExtendWith(MockitoExtension.class)
public class HomeOfficeMessageHeaderCreatorTest {

    private HomeOfficeMessageHeaderCreator homeOfficeMessageHeaderCreator;

    private ObjectMapper objectMapper;

    @Value("classpath:home-office-sample-response.json")
    private Resource resource;

    @BeforeEach
    void setUp() {
        homeOfficeMessageHeaderCreator = new HomeOfficeMessageHeaderCreator();
    }

    @Test
    public void create_home_office_header_returns_valid() {
        HttpHeaders headers = homeOfficeMessageHeaderCreator.getHomeOfficeHeader();

        assertNotNull(headers);
        assertThat(headers.getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
    }

    @Test
    public void createMessageHeader_returns_valid_values() {
        Map<String, Object> messageHeader = homeOfficeMessageHeaderCreator.createMessageHeader();

        assertNotNull(messageHeader);
        assertThat(messageHeader.size()).isEqualTo(3);
        assertNotNull(messageHeader.get("consumer"));
        assertNotNull(messageHeader.get("correlationId"));
        assertNotNull(messageHeader.get("eventDateTime"));
    }

    @Test
    public void createConsumer_returns_valid_values() {
        Map<String, String> consumer = homeOfficeMessageHeaderCreator.createConsumer();

        assertNotNull(consumer);
        assertThat(consumer.size()).isEqualTo(2);
        assertThat(consumer.get("code")).isEqualTo("HMCTS");
        assertThat(consumer.get("description")).isEqualTo("HM Courts and Tribunal Service");
    }

}
