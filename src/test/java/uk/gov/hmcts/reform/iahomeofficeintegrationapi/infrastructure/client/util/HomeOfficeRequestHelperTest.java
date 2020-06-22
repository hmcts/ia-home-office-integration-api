package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.util;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.util.FileCopyUtils;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.HomeOfficeResponseException;

@SpringJUnitConfig
@ExtendWith(MockitoExtension.class)
public class HomeOfficeRequestHelperTest {

    private HomeOfficeRequestHelper homeOfficeRequestHelper;

    private ObjectMapper objectMapper;

    @Value("classpath:home-office-sample-response.json")
    private Resource resource;

    @BeforeEach
    void setUp() {
        homeOfficeRequestHelper = new HomeOfficeRequestHelper();
    }

    @Test
    public void create_home_office_header_returns_valid() {
        HttpHeaders headers = homeOfficeRequestHelper.getHomeOfficeHeader();

        assertNotNull(headers);
        assertThat(headers.getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
    }

    @Test
    public void createMessageHeader_returns_valid_values() {
        Map<String, Object> messageHeader = homeOfficeRequestHelper.createMessageHeader();

        assertNotNull(messageHeader);
        assertThat(messageHeader.size()).isEqualTo(3);
        assertNotNull(messageHeader.get("consumer"));
        assertNotNull(messageHeader.get("correlationId"));
        assertNotNull(messageHeader.get("eventDateTime"));
    }

    @Test
    public void createConsumer_returns_valid_values() {
        Map<String, String> consumer = homeOfficeRequestHelper.createConsumer();

        assertNotNull(consumer);
        assertThat(consumer.size()).isEqualTo(2);
        assertThat(consumer.get("code")).isEqualTo("HMCTS");
        assertThat(consumer.get("description")).isEqualTo("HM Courts and Tribunal Service");
    }

    @Test
    public void call_extract_home_office_data_should_return_all_fields() throws Exception {

        Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8);
        String jsonResponse = FileCopyUtils.copyToString(reader);

        AsylumCase asylumCase = homeOfficeRequestHelper.extractHomeOfficeData(jsonResponse);

        assertNotNull(asylumCase);
        assertEquals(Optional.of("Smith"),
            asylumCase.read(AsylumCaseDefinition.HO_APPELLANT_FAMILY_NAME));
        assertEquals(Optional.of("Capability"),
            asylumCase.read(AsylumCaseDefinition.HO_APPELLANT_GIVEN_NAME));
        assertEquals(Optional.of("CAN (denoting Canada)"),
            asylumCase.read(AsylumCaseDefinition.HO_APPELLANT_NATIONALITY_CODE));
        assertEquals(Optional.of("Canada"),
            asylumCase.read(AsylumCaseDefinition.HO_APPELLANT_NATIONALITY));

    }

    @Test
    public void call_extract_home_office_data_should_throws_error_for_null_response() {
        assertThatThrownBy(() -> homeOfficeRequestHelper.extractHomeOfficeData(null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("Home office Appeal-validate response must not be null");
    }

    @Test
    public void call_extract_home_office_data_should_throws_error_for_null_person_element() {
        assertThatThrownBy(() -> homeOfficeRequestHelper.extractHomeOfficeData("{}"))
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    public void call_extract_home_office_data_should_throws_error_for_invalid_response() {
        assertThatThrownBy(() -> homeOfficeRequestHelper.extractHomeOfficeData(getErrorJsonResponseString()))
            .isExactlyInstanceOf(HomeOfficeResponseException.class)
            .hasMessage("Error test message");
    }

    @Test
    public void call_validate_response_throws_error_for_error_node_present() throws Exception {
        objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(getErrorJsonResponseString());
        assertThatThrownBy(() -> homeOfficeRequestHelper.hasValidHomeOfficeResponse(rootNode.get("errorDetails")))
            .isExactlyInstanceOf(HomeOfficeResponseException.class)
            .hasMessage("Error test message");
    }

    @Test
    public void call_date_time_string_returns_current_time_in_format() {
        String currentTime = HomeOfficeRequestHelper.getCurrentDateTime();
        assertNotNull(currentTime);
        assertEquals(20, currentTime.length());
    }

    private String getErrorJsonResponseString() {
        return "{"
            + "\"messageHeader\":{\"consumer\":"
            + " {\"code\":\"HMCTS\",\"description\":\"HM Courts and Tribunal Service\"},"
            + "\"correlationId\":\"ABC2344BCED2234EA\",\"eventDateTime\": \"2020-04-26T17:32:28Z\"},"
            + "\"errorDetails\": {\"errorCode\":\"1000\",\"message\":\"Error test message\"}"
            + "}";
    }

}
