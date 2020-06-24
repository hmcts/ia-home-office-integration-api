package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.util;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Service
public class HomeOfficeMessageHeaderCreator {

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
}
