package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.util;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.DateProvider;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.SystemDateProvider;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.HomeOfficeResponseException;

@Service
public class HomeOfficeMessageHeaderHelper {

    public static String getCurrentDateTime() {
        DateProvider dateProvider = new SystemDateProvider();
        return dateProvider.nowWithTime().format(
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));
    }

    public static String generateUuid() {
        return UUID.randomUUID().toString();
    }

    public HttpHeaders getHomeOfficeHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return headers;
    }

    public Map<String, Object> createMessageHeader() {
        Map<String, Object> messageHeader = new HashMap<>();
        messageHeader.put("consumer", createConsumer());
        messageHeader.put("correlationId", "ABC2344BCED2234EA");
        messageHeader.put("eventDateTime", "2020-05-19T17:32:28Z");

        return messageHeader;
    }

    Map<String, String> createConsumer() {
        return ImmutableMap.of("code", "HMCTS", "description", "HM Courts and Tribunal Service");
    }

    public AsylumCase extractHomeOfficeData(String response) {
        AsylumCase homeOfficeData = new AsylumCase();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            requireNonNull(response, "Home office Appeal-validate response must not be null");
            JsonNode hoJsonResponse = objectMapper.readTree(response);

            if (hasValidHomeOfficeResponse(hoJsonResponse.findPath("errorDetails"))) {
                requireNonNull(
                    hoJsonResponse.findPath("status").get(0).get("person"),
                    "Home office Appeal-validate response invalid");
                JsonNode person = objectMapper.readTree(response).findPath("status").get(0).get("person");
                homeOfficeData.write(AsylumCaseDefinition.HO_APPELLANT_FAMILY_NAME,
                    person.get("familyName").textValue());
                homeOfficeData.write(AsylumCaseDefinition.HO_APPELLANT_GIVEN_NAME,
                    person.get("givenName").textValue());
                homeOfficeData.write(AsylumCaseDefinition.HO_APPELLANT_NATIONALITY_CODE,
                    person.get("nationality").get("code").textValue());
                homeOfficeData.write(AsylumCaseDefinition.HO_APPELLANT_NATIONALITY,
                    person.get("nationality").get("description").textValue());
            }
        } catch (JsonProcessingException jpe) {
            throw new HomeOfficeResponseException("Home office Appeal-validate response invalid");
        }
        return homeOfficeData;
    }

    boolean hasValidHomeOfficeResponse(JsonNode errorDetailsNode) {
        if (errorDetailsNode != null && errorDetailsNode.get("errorCode") != null) {
            throw new HomeOfficeResponseException(
                errorDetailsNode.get("message").textValue()
            );
        }
        return true;
    }

}
