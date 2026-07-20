package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeApplicationDto;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.HomeOfficeApplicationApi;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.HomeOfficeMissingApplicationException;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.RetriesExceededException;
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

    public ResponseEntity<HomeOfficeApplicationDto> getApplication(
        String homeOfficeReferenceNumber) throws HomeOfficeMissingApplicationException {

        final String accessToken = accessTokenProvider.getAccessToken();
        String homeOfficeCorrelationId = HomeOfficeRequestUuidGenerator.generateUuid();
        String homeOfficeConsumer = homeOfficeProperties.getCodes().get("consumer").getCode();
        String homeOfficeEventDateTime = HomeOfficeDateFormatter.getCurrentDateTime();
        log.info(
            "Home Office /applications/v1/{} GET request will be sent with correlation ID {}, consumer code {} and event date-time {}.",
            homeOfficeReferenceNumber,
            homeOfficeCorrelationId,
            homeOfficeConsumer,
            homeOfficeEventDateTime
        );
        try {
            ResponseEntity<HomeOfficeApplicationDto> response = homeOfficeApplicationApi.getApplication(homeOfficeReferenceNumber,
                                                                                                        accessToken,
                                                                                                        homeOfficeCorrelationId,
                                                                                                        homeOfficeConsumer,
                                                                                                        homeOfficeEventDateTime);
            int statusCode = response.getStatusCode().value();
            log.info(
                "Home Office /applications/v1/{} GET response has been received with HTTP status {}.",
                homeOfficeReferenceNumber,
                statusCode
            );

            HomeOfficeApplicationDto body = response.getBody();
            if (body == null) {
                log.warn("Home Office /applications/v1/{} GET response body is null.", homeOfficeReferenceNumber);
            } else if (body.getAppellants() == null || body.getAppellants().isEmpty()) {
                log.warn("Home Office /applications/v1/{} GET response contained no appellants. UAN in response: {}",
                    homeOfficeReferenceNumber, body.getUan());
            } else {
                log.info("Home Office /applications/v1/{} GET response contained {} appellant(s). UAN in response: {}",
                    homeOfficeReferenceNumber, body.getAppellants().size(), body.getUan());
                body.getAppellants().forEach(appellant ->
                    log.info("  Appellant: {}", appellant)
                );
            }

            return response;
        } catch (RetriesExceededException e) {
            log.warn("Home Office /applications/v1/{} GET failed — retries exhausted: {}", homeOfficeReferenceNumber, e.getMessage());
            String message = "Biographic information from Home Office asylum (etc.) application with reference " + homeOfficeReferenceNumber
                           + " could not be retrieved.\n\nThe Home Office validation API did not respond.";
            throw new HomeOfficeMissingApplicationException(-1, message);
        }
    }
}
