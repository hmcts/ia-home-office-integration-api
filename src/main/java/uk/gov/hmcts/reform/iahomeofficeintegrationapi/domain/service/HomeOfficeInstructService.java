package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeInstruct;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeInstructResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.HomeOfficeInstructApi;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security.AccessTokenProvider;


@Service
@Slf4j
public class HomeOfficeInstructService {

    private final HomeOfficeInstructApi homeOfficeInstructApi;
    private final AccessTokenProvider accessTokenProvider;
    private final ObjectMapper objectMapper;

    public HomeOfficeInstructService(
        HomeOfficeInstructApi homeOfficeInstructApi,
        @Qualifier("homeOffice") AccessTokenProvider accessTokenProvider,
        ObjectMapper objectMapper) {
        this.homeOfficeInstructApi = homeOfficeInstructApi;
        this.accessTokenProvider = accessTokenProvider;
        this.objectMapper = objectMapper;
    }

    public String sendNotification(
        HomeOfficeInstruct request
    ) {

        final String accessToken = accessTokenProvider.getAccessToken();
        ObjectWriter objectWriter = this.objectMapper.writer().withDefaultPrettyPrinter();

        HomeOfficeInstructResponse instructResponse;
        String status;
        try {
            log.info("HomeOffice-Instruct request: {}", objectWriter.writeValueAsString(request));
            instructResponse = homeOfficeInstructApi.sendNotification(accessToken, request);
            log.info("HomeOffice-Instruct response: {}", objectWriter.writeValueAsString(instructResponse));

            if (instructResponse == null || instructResponse.getMessageHeader() == null) {
                log.error("Error sending notification to Home Office for reference ID {}", request.getHoReference());
                status = "FAIL";
            } else {
                status = "OK";
            }
        } catch (JsonProcessingException e) {
            log.error("Json error sending notification to Home office for reference {}, Message: {}: "
                      + e.getMessage());
            status = "FAIL";

        } catch (Exception e) {
            log.error("Error sending notification to Home office for reference {}, Message: {}",
                request.getHoReference(), e.getMessage());
            status = "FAIL";
        }

        return status;
    }

}
