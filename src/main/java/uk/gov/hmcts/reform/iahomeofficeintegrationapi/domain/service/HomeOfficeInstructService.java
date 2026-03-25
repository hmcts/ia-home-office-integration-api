package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeInstruct;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.MessageHeader;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ConsumerReference;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeErrorResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeInstruct;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.HomeOfficeInstructApi;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.RetriesExceededException;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security.AccessTokenProvider;


@Service
@Slf4j
public class HomeOfficeInstructService {

    private final HomeOfficeInstructApi homeOfficeInstructApi;
    private final AccessTokenProvider accessTokenProvider;

    public HomeOfficeInstructService(
        HomeOfficeInstructApi homeOfficeInstructApi,
        @Qualifier("homeOffice") AccessTokenProvider accessTokenProvider) {
        this.homeOfficeInstructApi = homeOfficeInstructApi;
        this.accessTokenProvider = accessTokenProvider;
    }

    public String sendNotification(
        HomeOfficeInstruct request
    ) {

        HomeOfficeErrorResponse instructResponse;
        String status;
        final String correlationId = request.getMessageHeader().getCorrelationId();
        final String caseId = request.getConsumerReference().getValue();
        final String homeOfficeReferenceNumber = request.getHoReference();
        final String messageType = request.getMessageType();
        try {
            final String accessToken = accessTokenProvider.getAccessToken();
            log.info(
                "HomeOffice-Notification request is to be sent for caseId: {},  reference number: {}, "
                + "message type: {} and correlation ID: {}",
                caseId,
                homeOfficeReferenceNumber,
                messageType,
                correlationId
            );
            log.info("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            log.info("*Request info*\n  hoReference: {}\n  messageType: {}\n  note: {}", 
                request.getHoReference(), request.getMessageType(), request.getNote());
            MessageHeader rmessageHeader = request.getMessageHeader();
            log.info("*Message header*\n  eventDateTime: {}\n  correlationId: {}\n  consumerCode: {}\n" +
                "  consumerDescription: {}",
                rmessageHeader.getEventDateTime(), rmessageHeader.getCorrelationId(), 
                rmessageHeader.getConsumer().getCode(), rmessageHeader.getConsumer().getDescription());
            ConsumerReference consumerReference = request.getConsumerReference();
            log.info("*Consumer reference*\n  code: {}\n  description: {}\n  value: {}\n" +
                "  consumerCode: {}\n  consumerDescription: {}", 
                consumerReference.getCode(), consumerReference.getDescription(), consumerReference.getValue(), 
                consumerReference.getConsumer().getCode(), consumerReference.getConsumer().getDescription());
            log.info("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            instructResponse = homeOfficeInstructApi.sendNotification(accessToken, request);
            var errorDetail = instructResponse.getErrorDetail();
            if (errorDetail != null) {
                log.info("**>>isSuccess: {}<<**\n**>>errorCode: {}<<**\n**>>messageText: {}<<**", 
                    errorDetail.isSuccess(), errorDetail.getErrorCode(), errorDetail.getMessageText());
            }
            var messageHeader = instructResponse.getMessageHeader();
            if (messageHeader != null) {
                log.info("**--correlationId: {}--**\n**--eventDateTime: {}--**\n**--consumerCode: {}--**\n**--consumerDescription: {}--**\n", 
                    messageHeader.getCorrelationId(), messageHeader.getEventDateTime(), messageHeader.getConsumer().getCode(), 
                    messageHeader.getConsumer().getDescription());
            }

            if (instructResponse == null || instructResponse.getMessageHeader() == null) {
                log.info("Response was null or header was null");
                log.error("Error sending notification to Home Office for caseId: {},  reference number: {}, "
                          + "message type: {} and correlation ID: {}",
                    caseId,
                    homeOfficeReferenceNumber,
                    messageType,
                    correlationId
                );
                status = "FAIL";
            } else {
                log.info("Response was NOT null and header was NOT null");
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
            log.info("Retry count exceeded");
            log.error("Server error sending notification to Home office for caseId: {},  reference number: {}, "
                      + "message type: {} and correlation ID: {}, Message: {}: ",
                caseId,
                homeOfficeReferenceNumber,
                messageType,
                correlationId,
                e.getMessage());
            status = "FAIL";

        } catch (Exception e) {
            log.error("Error sending notification to Home office for caseId: {},  reference number: {}, "
                      + "message type: {} and correlation ID: {}, Message: {}: ",
                caseId,
                homeOfficeReferenceNumber,
                messageType,
                correlationId,
                e.getMessage());

            status = "FAIL";
        }

        return status;
    }

}
