package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeApplicationDto;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.HomeOfficeApplicationApi;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.HomeOfficeMissingApplicationException;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.util.HomeOfficeDateFormatter;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.util.HomeOfficeRequestUuidGenerator;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.config.HomeOfficeProperties;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security.AccessTokenProvider;


@Service
@Slf4j
public class HomeOfficeApplicationService {

    private final HomeOfficeProperties homeOfficeProperties;
    private final HomeOfficeApplicationApi homeOfficeApplicationApi;
    private final AccessTokenProvider accessTokenProvider;

    public HomeOfficeApplicationService(
        HomeOfficeProperties homeOfficeProperties,
        HomeOfficeApplicationApi homeOfficeApplicationApi,
        @Qualifier("homeOffice") AccessTokenProvider accessTokenProvider) {
        this.homeOfficeProperties = homeOfficeProperties;
        this.homeOfficeApplicationApi = homeOfficeApplicationApi;
        this.accessTokenProvider = accessTokenProvider;
    }

    public HomeOfficeApplicationDto getApplication(
        String homeOfficeReferenceNumber) throws HomeOfficeMissingApplicationException {

        final String accessToken = accessTokenProvider.getAccessToken();
        String homeOfficeCorrelationId = HomeOfficeRequestUuidGenerator.generateUuid();
        String homeOfficeConsumer = homeOfficeProperties.getCodes().get("consumer").getCode();
        String homeOfficeEventDateTime = HomeOfficeDateFormatter.getCurrentDateTime();
        log.info(
            "Home Office /applications/v1/{id} GET request will be sent with correlation ID {}, consumer code {} and event date-time {}.",
            homeOfficeReferenceNumber,
            homeOfficeCorrelationId,
            homeOfficeConsumer,
            homeOfficeEventDateTime
        );
        ResponseEntity<HomeOfficeApplicationDto> response;
        try {
            response = homeOfficeApplicationApi.getApplication(homeOfficeReferenceNumber, accessToken, homeOfficeCorrelationId, homeOfficeConsumer, homeOfficeEventDateTime);
            int statusCode = response.getStatusCodeValue();
            log.info(
                "Home Office /applications/v1/{id} GET response has been received with HTTP status {}.",
                homeOfficeReferenceNumber,
                statusCode
            );
            return response.getBody();
        } catch (FeignException e) {
            int statusCode = e.status();
            String responseBody = e.contentUTF8();
            log.info(
                "Home Office /applications/v1/{id} GET response has been received with HTTP status {}.  The body of the response is:\n\n{}",
                homeOfficeReferenceNumber,
                statusCode,
                responseBody
            );
            // Throw new exception to be caught by the event handler
            String message = "Biographic information from Home Office application with HMCTS reference " + homeOfficeReferenceNumber + " could not be retrieved.";
            switch (statusCode) {
                case -1:
                    message += "\n\nThe Home Office validation API did not respond.";
                    break;
                case 400:
                    message += "\n\nThe request to the Home Office validation API was not correctly formed.";
                    break;
                case 401:
                    message += "\n\nThe request to the Home Office validation API could not be authenticated.";
                    break;
                case 403:
                    message += "\n\nThe request to the Home Office validation API was authenticated but not authorised.";
                    break;
                case 404:
                    message += "\n\nNo application matching this HMCTS reference number was found.";
                    break;
                case 500:
                case 501:
                case 502:
                case 503:
                case 504:
                    message += "\n\nThe Home Office validation API was not available.";
                    break;            
                default:
                    message += "\n\nThe HTTP status code was " + String.valueOf(statusCode) + ".";
                    break;
            }
            throw new HomeOfficeMissingApplicationException(statusCode, message);
        }
    }
}
