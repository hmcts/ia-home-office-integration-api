package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.service;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeStatutoryTimeframeDto;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.CaseDataContent;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.StartEventDetails;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.StatutoryTimeframe24Weeks;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.SubmitEventDetails;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.CcdDataApi;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security.idam.IdentityManagerResponseException;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.IdamService;

import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.STATUTORY_TIMEFRAME_24_WEEKS;


@Service
@Slf4j
public class CcdDataService {

    private final CcdDataApi ccdDataApi;
    private final IdamService idamService;
    private final AuthTokenGenerator serviceAuthorization;

    private static final String JURISDICTION = "IA";
    private static final String CASE_TYPE = "Asylum";
    private static final int DEFAULT_SUCCESS_STATUS_CODE = 200;

    @Value("${core_case_data_api_url}")
    private String coreCaseDataApiUrl;


    public CcdDataService(CcdDataApi ccdDataApi,
                          IdamService systemTokenGenerator,
                          AuthTokenGenerator serviceAuthorization) {

        this.ccdDataApi = ccdDataApi;
        this.idamService = systemTokenGenerator;

        this.serviceAuthorization = serviceAuthorization;
    }

    public SubmitEventDetails setHomeOfficeStatutoryTimeframeStatus(HomeOfficeStatutoryTimeframeDto hoStatutoryTimeframeDto) {

        String eventId = Event.SET_HOME_OFFICE_STATUTORY_TIMEFRAME_STATUS.toString();
        String caseId = hoStatutoryTimeframeDto.getCcdCaseNumber();

        String userToken;
        String s2sToken;
        String userId;
        try {
            userToken = "Bearer " + idamService.getServiceUserToken();
            log.info("A System user token has been generated for event: {}, caseId: {}.", eventId, caseId);

            s2sToken = serviceAuthorization.generate();
            log.info("S2S token has been generated for event: {}, caseId: {}.", eventId, caseId);

            userId = idamService.getUserInfo(userToken).getUid();
            log.info("System user id has been fetched for event: {}, caseId: {}.", eventId, caseId);

        } catch (IdentityManagerResponseException ex) {
            log.error("Unauthorised access to getCaseById", ex.getMessage());
            throw new IdentityManagerResponseException(ex.getMessage(), ex);
        }

                
        log.info("ccd url: {}", coreCaseDataApiUrl);
        final StartEventDetails startEventDetails = getStartEventByCase(userToken, s2sToken, caseId, eventId);
        log.info("Case details found for the caseId: {}", caseId);
        log.info("Start event details token: {}", startEventDetails.getToken());
        log.info("Start event details id: {}", startEventDetails.getEventId());
        CaseDetails<AsylumCase> caseDetails = startEventDetails.getCaseDetails();
        if (caseDetails == null) {
            log.error("Case details is null for caseId: {}", caseId);
            throw new IllegalStateException("Case details is null for caseId: " + caseId);
        } else {
            log.info("Start case details id: {}", caseDetails.getId());
            log.info("Start case details state: {}", caseDetails.getState());
            log.info("Start case details created date: {}", caseDetails.getCreatedDate());
            log.info("Start case details data: {}", caseDetails.getCaseData());
        }


        Map<String, Object> eventData = new HashMap<>();
        eventData.put(STATUTORY_TIMEFRAME_24_WEEKS.value(), toStf4w("1", hoStatutoryTimeframeDto));
   
        log.info("Event data to be submitted: {}", eventData);    
        log.info("Submitting event with method: {} for caseId: {} with Home Office statutory timeframe status: {}", eventId, caseId,
                 hoStatutoryTimeframeDto.isHoStatutoryTimeframeStatus());
        
        SubmitEventDetails submitEventDetails = submitEvent(userToken, s2sToken, caseId, eventData, startEventDetails.getToken(), eventId, true);

        log.info("Home Office statutory timeframe status updated for the caseId: {}, Status: {}, Message: {}", caseId,
                 submitEventDetails.getCallbackResponseStatusCode(), submitEventDetails.getCallbackResponseStatus());

        return submitEventDetails;
    }

    private StartEventDetails getStartEvent(
        String userToken, String s2sToken, String uid, String jurisdiction, String caseType, String caseId, String eventId) {
        //log all the arguments
        log.info("Getting case with userToken: {}, s2sToken: {}, uid: {}, jurisdiction: {}, caseType: {}, caseId: {}, EventId: {}",
                 userToken, s2sToken, uid, jurisdiction, caseType, caseId, eventId);
        return ccdDataApi.startEvent(userToken, s2sToken, uid, jurisdiction, caseType,
                                     caseId, eventId);
    }

    private StartEventDetails getStartEventByCase(
        String userToken, String s2sToken, String caseId, String eventId) {
        log.info("Getting start event by case with userToken: {}, s2sToken: {}, caseId: {}, EventId: {}",
                 userToken, s2sToken, caseId, eventId);
        return ccdDataApi.startEventByCase(userToken, s2sToken, caseId, eventId);
    }

    private SubmitEventDetails submitEvent(
        String userToken, String s2sToken, String caseId, Map<String, Object> eventData,
        String eventToken, String eventId, boolean ignoreWarning) {

        log.info("Event data to be submitted: {}", eventData);
        
        // Construct the event metadata
        Map<String, Object> eventMetadata = new HashMap<>();
        eventMetadata.put("id", eventId);
        eventMetadata.put("summary", "");
        eventMetadata.put("description", "");
        
        // This object will be serialized to JSON by Feign/Jackson
        // Example JSON output:
        // {
        //   "data": {
        //     "statutoryTimeframe24Weeks": [
        //       {
        //         "id": "1",
        //         "value": {
        //           "status": "Yes",
        //           "reason": "Home Office statutory timeframe update",
        //           "user": "Home Office Integration API",
        //           "dateAdded": "2023-12-01T00:00:00Z"
        //         }
        //       }
        //     ]
        //   },
        //   "event": {
        //     "id": "addStatutoryTimeframe24Weeks",
        //     "summary": "",
        //     "description": ""
        //   },
        //   "event_token": "test-event-token",
        //   "ignore_warning": false
        // }
        
        CaseDataContent requestBody =
            new CaseDataContent(caseId, eventData, eventMetadata, eventToken, ignoreWarning);

        log.info("CaseDataContent Request - caseReference: {}", requestBody.getCaseReference());
        log.info("CaseDataContent Request - data: {}", requestBody.getData());
        log.info("CaseDataContent Request - event: {}", requestBody.getEvent());
        log.info("CaseDataContent Request - eventToken: {}", requestBody.getEventToken());
        log.info("CaseDataContent Request - ignoreWarning: {}", requestBody.isIgnoreWarning());
        
        log.info("Submitting case with caseId: {}, eventData: {}, eventToken: {}, ignoreWarning: {}",
                 caseId, eventData, eventToken, ignoreWarning);
        
        // Feign client automatically serializes requestBody to JSON using Jackson
        // The @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class) annotation
        // converts camelCase field names to snake_case in the JSON
        return ccdDataApi.submitEvent(userToken, s2sToken, caseId, requestBody);
    }

    private SubmitEventDetails submitEventForCaseWorker(
        String userToken, String s2sToken, String userId, String caseId, Map<String, Object> eventData,
        String eventToken, String eventId, boolean ignoreWarning) {

        log.info("Event data to be submitted: {}", eventData);
        
        // Construct the event metadata
        Map<String, Object> eventMetadata = new HashMap<>();
        eventMetadata.put("id", eventId);
        eventMetadata.put("summary", "");
        eventMetadata.put("description", "");
        
        CaseDataContent request =
            new CaseDataContent(caseId, eventData, eventMetadata, eventToken, ignoreWarning);

        log.info("CaseDataContent Request - caseReference: {}", request.getCaseReference());
        log.info("CaseDataContent Request - data: {}", request.getData());
        log.info("CaseDataContent Request - event: {}", request.getEvent());
        log.info("CaseDataContent Request - eventToken: {}", request.getEventToken());
        log.info("CaseDataContent Request - ignoreWarning: {}", request.isIgnoreWarning());
        
        log.info("Submitting case for caseworker with userId: {}, caseId: {}, eventData: {}, eventToken: {}, ignoreWarning: {}",
                 userId, caseId, eventData, eventToken, ignoreWarning);
        
        CaseDetails caseDetails = ccdDataApi.submitEventForCaseWorker(userToken, s2sToken, userId, JURISDICTION, CASE_TYPE, caseId, ignoreWarning, request);
        
        log.info("Case details returned from submitEventForCaseWorker - id: {}", caseDetails.getId());
        log.info("Case details returned from submitEventForCaseWorker - jurisdiction: {}", caseDetails.getJurisdiction());
        log.info("Case details returned from submitEventForCaseWorker - state: {}", caseDetails.getState());
        log.info("Case details returned from submitEventForCaseWorker - callbackResponseStatus: {}", caseDetails.getCallbackResponseStatus());
        log.info("Case details returned from submitEventForCaseWorker - caseData: {}", caseDetails.getCaseData());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> updatedCaseData = (Map<String, Object>) caseDetails.getCaseData();
        
        return new SubmitEventDetails(
            caseDetails.getId(),
            caseDetails.getJurisdiction(),
            caseDetails.getState(),
            updatedCaseData,
            DEFAULT_SUCCESS_STATUS_CODE,
            caseDetails.getCallbackResponseStatus()
        );
    }

    public List<IdValue<StatutoryTimeframe24Weeks>> toStf4w(String id, HomeOfficeStatutoryTimeframeDto hoStatutoryTimeframeDto) {
        
        YesOrNo status = hoStatutoryTimeframeDto.isHoStatutoryTimeframeStatus() ? YesOrNo.YES : YesOrNo.NO;
        String reason = "Home Office statutory timeframe update";
        String user = "Home Office Integration API";
        String dateTimeAdded = hoStatutoryTimeframeDto.getTimeStamp().format(DateTimeFormatter.ISO_LOCAL_DATE) + "T00:00:00Z";
        
        StatutoryTimeframe24Weeks statutoryTimeframeValue = new StatutoryTimeframe24Weeks(
            status,
            reason,
            user,
            dateTimeAdded
        );
        
        IdValue<StatutoryTimeframe24Weeks> idValue = new IdValue<>(id, statutoryTimeframeValue);
        
        return List.of(idValue);
    }
}
