package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.StatutoryTimeframe24WeeksHistory;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.SubmitEventDetails;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.CaseNotFoundException;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.CcdDataApi;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security.idam.IdentityManagerResponseException;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.IdamService;

import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.STATUTORY_TIMEFRAME_24_WEEKS;

@Service
@Slf4j
public class CcdDataService {

    private static final String STATUTORY_TIMEFRAME_REASON = "Home Office initial determination";
    private static final String STATUTORY_TIMEFRAME_USER = "Home Office Integration API";
    private static final String STATUTORY_TIMEFRAME_24_WEEKS_REASON_FIELD = "statutoryTimeframe24WeeksReason";
    private static final String STATUTORY_TIMEFRAME_24_WEEKS_HOME_OFFICE_CASE_TYPE_FIELD = "statutoryTimeframe24WeeksHomeOfficeCaseType";
    private static final String EVENT_METADATA_ID_KEY = "id";
    private static final String EVENT_METADATA_SUMMARY_KEY = "summary";
    private static final String EVENT_METADATA_DESCRIPTION_KEY = "description";

    private final CcdDataApi ccdDataApi;
    private final IdamService idamService;
    private final AuthTokenGenerator serviceAuthorization;

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

        boolean isStatusNo = hoStatutoryTimeframeDto.getStf24weeks().getStatus().equalsIgnoreCase("no");
        Event event = isStatusNo 
            ? Event.REMOVE_STATUTORY_TIMEFRAME_24_WEEKS 
            : Event.SET_HOME_OFFICE_STATUTORY_TIMEFRAME_STATUS;
        String eventId = event.toString();
        String caseId = String.valueOf(hoStatutoryTimeframeDto.getCcdCaseId());

        String userToken;
        String s2sToken;
        try {
            userToken = "Bearer " + getServiceUserToken();
            log.info("A System user token has been generated for event: {}, caseId: {}.", eventId, caseId);

            s2sToken = generateS2SToken();
            log.info("S2S token has been generated for event: {}, caseId: {}.", eventId, caseId);

        } catch (IdentityManagerResponseException ex) {
            log.error("Unauthorised access to getCaseById", ex.getMessage());
            throw new IdentityManagerResponseException(ex.getMessage(), ex);
        }
        
        log.debug("ccd url: {}", coreCaseDataApiUrl);
        final StartEventDetails startEventDetails = getStartEventByCase(userToken, s2sToken, caseId, eventId);
        log.info("Case details found for the caseId: {}", caseId);
        log.info("Start event id: {}", startEventDetails.getEventId());
        CaseDetails<AsylumCase> caseDetails = startEventDetails.getCaseDetails();
        if (caseDetails == null) {
            log.error("Case details is null for caseId: {}", caseId);
            throw new IllegalStateException("Case details is null for caseId: " + caseId);
        } else {
            log.info("Start case details id: {}", caseDetails.getId());
            log.info("Start case details state: {}", caseDetails.getState());
            log.info("Start case details created date: {}", caseDetails.getCreatedDate());
            AsylumCase asylumCase = caseDetails.getCaseData();
            log.debug("Start case details data: {}", asylumCase);

            Optional<StatutoryTimeframe24Weeks> existingData = asylumCase.read(STATUTORY_TIMEFRAME_24_WEEKS);
            String newHistoryId = nextHistoryId(existingData);

            checkStatusNotAlreadySet(newHistoryId, existingData, caseId);

            Map<String, Object> eventData = new HashMap<>();
            eventData.put(STATUTORY_TIMEFRAME_24_WEEKS.value(), toStf4w(newHistoryId, hoStatutoryTimeframeDto));
            eventData.put(STATUTORY_TIMEFRAME_24_WEEKS_REASON_FIELD, STATUTORY_TIMEFRAME_REASON);
            String homeCaseType = hoStatutoryTimeframeDto.getStf24weeks().getCaseType();
            eventData.put(STATUTORY_TIMEFRAME_24_WEEKS_HOME_OFFICE_CASE_TYPE_FIELD, homeCaseType);
       
            log.debug("Event data to be submitted: {}", eventData);    
            log.info("Submitting event with method: {} for caseId: {} with Home Office statutory timeframe status: {}, caseType: {}", 
                     eventId, caseId,
                     hoStatutoryTimeframeDto.getStf24weeks().getStatus(),
                     homeCaseType);
            
            SubmitEventDetails submitEventDetails = submitEvent(userToken, s2sToken, caseId, eventData, startEventDetails.getToken(), eventId, true);

            log.info("Home Office statutory timeframe status updated for the caseId: {}, Status: {}, Message: {}", caseId,
                     submitEventDetails.getCallbackResponseStatusCode(), submitEventDetails.getCallbackResponseStatus());

            return submitEventDetails;
        }
    }

    private StartEventDetails getStartEventByCase(
        String userToken, String s2sToken, String caseId, String eventId) {
        log.info("Getting start event by case with caseId: {}, EventId: {}", caseId, eventId);
        try {
            return ccdDataApi.startEventByCase(userToken, s2sToken, caseId, eventId);
        } catch (Exception ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("Case ID is not valid")) {
                log.error("Case not found for caseId: {}", caseId);
                throw new CaseNotFoundException("Case not found for caseId: " + caseId);
            }
            throw ex;
        }
    }

    private SubmitEventDetails submitEvent(
        String userToken, String s2sToken, String caseId, Map<String, Object> eventData,
        String eventToken, String eventId, boolean ignoreWarning) {

        log.info("Event data to be submitted: {}", eventData);
        
        Map<String, Object> eventMetadata = new HashMap<>();
        eventMetadata.put(EVENT_METADATA_ID_KEY, eventId);
        eventMetadata.put(EVENT_METADATA_SUMMARY_KEY, "");
        eventMetadata.put(EVENT_METADATA_DESCRIPTION_KEY, "");
        
        CaseDataContent requestBody =
            new CaseDataContent(caseId, eventData, eventMetadata, eventToken, ignoreWarning);

        log.debug("CaseDataContent Request - caseReference: {}", requestBody.getCaseReference());
        log.debug("CaseDataContent Request - data: {}", requestBody.getData());
        log.debug("CaseDataContent Request - event: {}", requestBody.getEvent());
        log.debug("CaseDataContent Request - ignoreWarning: {}", requestBody.isIgnoreWarning());
        
        log.info("Submitting case with caseId: {}, eventData: {}, ignoreWarning: {}",
                 caseId, eventData, ignoreWarning);
        
        return ccdDataApi.submitEventByCase(userToken, s2sToken, caseId, requestBody);
    }

    public StatutoryTimeframe24Weeks toStf4w(String id, HomeOfficeStatutoryTimeframeDto hoStatutoryTimeframeDto) {
        
        boolean isYes = hoStatutoryTimeframeDto.getStf24weeks().getStatus().equalsIgnoreCase("yes");
        YesOrNo status = isYes ? YesOrNo.YES : YesOrNo.NO;
        String homeOfficeCaseType = hoStatutoryTimeframeDto.getStf24weeks().getCaseType();
        String dateTimeAdded = hoStatutoryTimeframeDto.getTimeStamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        
        StatutoryTimeframe24WeeksHistory historyEntry = new StatutoryTimeframe24WeeksHistory(
            status,
            STATUTORY_TIMEFRAME_REASON,
            homeOfficeCaseType,
            STATUTORY_TIMEFRAME_USER,
            dateTimeAdded
        );

        List<IdValue<StatutoryTimeframe24WeeksHistory>> historyList = new ArrayList<>();
        historyList.add(new IdValue<>(id, historyEntry));

        log.info("new StatutoryTimeframe24Weeks created with status: {}, homeOfficeCaseType: {}, history size: {}",
                 status, homeOfficeCaseType, historyList.size());

        return new StatutoryTimeframe24Weeks(
            status,
            homeOfficeCaseType,
            historyList
        );
        
    }

    public String nextHistoryId(Optional<StatutoryTimeframe24Weeks> existingData) {
        if (existingData.isEmpty()) {
            log.info("No existing statutory timeframe 24 weeks data found, returning historyId: 1");
            return "1";
        }
        
        List<IdValue<StatutoryTimeframe24WeeksHistory>> existingHistory = 
            existingData.get().getHistory();
        
        if (existingHistory == null || existingHistory.isEmpty()) {
            log.info("Existing statutory timeframe 24 weeks data has no history, returning historyId: 1");
            return "1";
        }
        
        int maxId = existingHistory.stream()
            .map(IdValue::getId)
            .mapToInt(Integer::parseInt)
            .max()
            .orElse(0);
        
        String nextId = String.valueOf(maxId + 1);
        log.info("Found {} existing history entries, max ID: {}, returning next historyId: {}", 
                 existingHistory.size(), maxId, nextId);
        
        return nextId;
    }

    private void checkStatusNotAlreadySet(
        String newHistoryId,
        Optional<StatutoryTimeframe24Weeks> existingData,
        String caseId) {
        
        if (!newHistoryId.equals("1")) {
            YesOrNo existingStatus = existingData.get().getCurrentStatusAutoGenerated();
            String existingCaseType = existingData.get().getCurrentHomeOfficeCaseTypeAutoGenerated();
            
            String errorMessage = String.format(
                "Statutory timeframe status has already been set to '%s' for case type '%s' for caseId: %s",
                existingStatus,
                existingCaseType,
                caseId
            );
            log.error(errorMessage);
            throw new IllegalStateException(errorMessage);
        }
    }

    public String generateS2SToken() {
        log.info("Generating S2S token");
        String s2sToken = serviceAuthorization.generate();
        log.info("S2S token generated successfully");
        return s2sToken;
    }

    public String getServiceUserToken() {
        log.info("Generating service user token");
        String serviceUserToken = idamService.getServiceUserToken();
        log.info("Service user token generated successfully");
        return serviceUserToken;
    }
}
