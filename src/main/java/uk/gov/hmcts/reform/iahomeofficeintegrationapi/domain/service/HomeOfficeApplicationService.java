package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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
        ResponseEntity<HomeOfficeApplicationDto> response = homeOfficeApplicationApi.getApplication(homeOfficeReferenceNumber, 
                                                                                                    accessToken, 
                                                                                                    homeOfficeCorrelationId, 
                                                                                                    homeOfficeConsumer, 
                                                                                                    homeOfficeEventDateTime);
        int statusCode = response.getStatusCodeValue();
        log.info(
            "Home Office /applications/v1/{id} GET response has been received with HTTP status {}.",
            homeOfficeReferenceNumber,
            statusCode
        );
        return response.getBody();
    }
}
