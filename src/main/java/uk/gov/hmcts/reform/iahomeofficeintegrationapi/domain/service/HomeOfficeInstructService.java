package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeInstruct;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeInstructResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.HomeOfficeInstructApi;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.RetriesExceededException;
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

        HomeOfficeInstructResponse instructResponse;
        String status;
        final String correlationId = request.getMessageHeader().getCorrelationId();
        final String caseId = request.getConsumerReference().getValue();
        final String homeOfficeReferenceNumber = request.getHoReference();
        final String messageType = request.getMessageType();
        try {
            final String accessToken = accessTokenProvider.getAccessToken();
            ObjectWriter objectWriter = this.objectMapper.writer().withDefaultPrettyPrinter();
            log.info(
                "HomeOffice-Notification request is to be sent for caseId: {},  reference number: {}, "
                + "message type: {} and correlation ID: {}",
                caseId,
                homeOfficeReferenceNumber,
                messageType,
                correlationId
            );
            instructResponse = homeOfficeInstructApi.sendNotification(accessToken, request);

            if (instructResponse == null || instructResponse.getMessageHeader() == null) {
                log.error("Error sending notification to Home Office for caseId: {},  reference number: {}, "
                          + "message type: {} and correlation ID: {}",
                    caseId,
                    homeOfficeReferenceNumber,
                    messageType,
                    correlationId
                );
                status = "FAIL";
            } else {
                log.info(
                    "HomeOffice-Notification request response received for caseId: {},  reference number: {}, "
                    + "message type: {} and correlation ID: {}",
                    caseId,
                    homeOfficeReferenceNumber,
                    messageType,
                    correlationId
                );
                status = "OK";
            }
        } catch (RetriesExceededException e) {
            log.error("Server error sending notification to Home office for caseId: {},  reference number: {}, "
                      + "message type: {} and correlation ID: {}, Message: {}: ",
                caseId,
                homeOfficeReferenceNumber,
                messageType,
                correlationId,
                e.getMessage(),
                e);
            status = "FAIL";

        } catch (Exception e) {
            log.error("Error sending notification to Home office for caseId: {},  reference number: {}, "
                      + "message type: {} and correlation ID: {}, Message: {}: ",
                caseId,
                homeOfficeReferenceNumber,
                messageType,
                correlationId,
                e.getMessage(),
                e);

            status = "FAIL";
        }

        return status;
    }

}
