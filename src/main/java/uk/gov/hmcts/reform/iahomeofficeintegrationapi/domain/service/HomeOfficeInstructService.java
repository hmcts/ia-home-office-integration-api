package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service;

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

    private static final String FAIL_STATUS = "FAIL";
    private static final String OK_STATUS = "OK";
    private static final String LOG_MESSAGE_ENDING = " for caseId: {},  reference number: {}, "
            + "message type: {} and correlation ID: {}";

    private final HomeOfficeInstructApi homeOfficeInstructApi;
    private final AccessTokenProvider accessTokenProvider;

    public HomeOfficeInstructService(
        HomeOfficeInstructApi homeOfficeInstructApi,
        @Qualifier("homeOffice") AccessTokenProvider accessTokenProvider) {
        this.homeOfficeInstructApi = homeOfficeInstructApi;
        this.accessTokenProvider = accessTokenProvider;
    }

    public String sendNotification(HomeOfficeInstruct request) {
        String notificationStatus;
        final String correlationId = request.getMessageHeader().getCorrelationId();
        final String caseId = request.getConsumerReference().getValue();
        final String homeOfficeReferenceNumber = request.getHoReference();
        final String messageType = request.getMessageType();
        try {
            final String accessToken = accessTokenProvider.getAccessToken();
            log.info(
                "HomeOffice-Notification request is to be sent" + LOG_MESSAGE_ENDING,
                caseId,
                homeOfficeReferenceNumber,
                messageType,
                correlationId
            );
            HomeOfficeInstructResponse instructResponse = homeOfficeInstructApi.sendNotification(accessToken, request);

            if (instructResponse == null || instructResponse.getMessageHeader() == null) {
                log.error("Error sending notification to Home Office" + LOG_MESSAGE_ENDING,
                    caseId,
                    homeOfficeReferenceNumber,
                    messageType,
                    correlationId
                );
                notificationStatus = FAIL_STATUS;
            } else {
                log.info(
                    "HomeOffice-Notification request response received" + LOG_MESSAGE_ENDING,
                    caseId,
                    homeOfficeReferenceNumber,
                    messageType,
                    correlationId
                );
                notificationStatus = OK_STATUS;
            }
        } catch (Exception e) {
            logError(e, caseId, homeOfficeReferenceNumber, messageType, correlationId);
            notificationStatus = FAIL_STATUS;
        }

        return notificationStatus;
    }

    private static void logError(Exception e, String caseId, String homeOfficeReferenceNumber, String messageType, String correlationId) {
        String errorMessage = e instanceof RetriesExceededException
                ? "Server error sending notification to Home office"
                : "Error sending notification to Home office";

        log.error(errorMessage + LOG_MESSAGE_ENDING + ", Message: {}: ",
                caseId,
                homeOfficeReferenceNumber,
                messageType,
                correlationId,
                e.getMessage(),
                e);
    }

}
