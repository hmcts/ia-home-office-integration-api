package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.DbUtils;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.CcdDataApi;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security.idam.IdentityManagerResponseException;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.IdamService;

import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.STATUTORY_TIMEFRAME_24_WEEKS;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.STF_24W_CURRENT_REASON_AUTO_GENERATED;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.STF_24W_HOME_OFFICE_COHORT;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.STF_24W_CURRENT_STATUS_AUTO_GENERATED;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.STF_24W_PREVIOUS_STATUS_WAS_YES_AUTO_GENERATED;

@Service
@Slf4j
public class CcdDataService {

    private static final String STATUTORY_TIMEFRAME_REASON = "Home Office initial determination";
    private static final String STATUTORY_TIMEFRAME_USER = "Home Office Integration API";

    private static final String EVENT_METADATA_ID_KEY = "id";
    private static final String EVENT_METADATA_SUMMARY_KEY = "summary";
    private static final String EVENT_METADATA_DESCRIPTION_KEY = "description";

    private final CcdDataApi ccdDataApi;
    private final IdamService idamService;
    private final AuthTokenGenerator serviceAuthorization;
    private final DbUtils dbUtils;

    @Value("${core_case_data_api_url}")
    private String coreCaseDataApiUrl;

    public CcdDataService(CcdDataApi ccdDataApi,
                          IdamService systemTokenGenerator,
                          AuthTokenGenerator serviceAuthorization,
                          DbUtils dbUtils) {

        this.ccdDataApi = ccdDataApi;
        this.idamService = systemTokenGenerator;
        this.serviceAuthorization = serviceAuthorization;
        this.dbUtils = dbUtils;
    }

    public SubmitEventDetails setHomeOfficeStatutoryTimeframeStatus(HomeOfficeStatutoryTimeframeDto hoStatutoryTimeframeDto) {
        // This caters for cases where no cohort information is returned (which we interpret as "No")
        boolean isYes = hoStatutoryTimeframeDto.getStf24weekCohorts().stream().anyMatch(cohort -> Boolean.parseBoolean(cohort.getIncluded()));
        Event event = isYes 
            ? Event.SET_HOME_OFFICE_STATUTORY_TIMEFRAME_STATUS
            : Event.REMOVE_STATUTORY_TIMEFRAME_24_WEEKS;
        String eventId = event.toString();
        String caseId = getCaseIdFromHmctsRefNum(String.valueOf(hoStatutoryTimeframeDto.getHmctsReferenceNumber()));

        String userToken;
        String s2sToken;
        try {
            userToken = "Bearer " + idamService.getServiceUserToken();
            log.debug("A System user token has been generated for event: {}, caseId: {}.", eventId, caseId);

            s2sToken = serviceAuthorization.generate();
            log.debug("S2S token has been generated for event: {}, caseId: {}.", eventId, caseId);

        } catch (IdentityManagerResponseException ex) {
            log.info("Unauthorised access to getCaseById", ex.getMessage());
            throw new IdentityManagerResponseException(ex.getMessage(), ex);
        }
        
        final StartEventDetails startEventDetails = getStartEventByCase(userToken, s2sToken, caseId, eventId);
        log.debug("Case details found for the caseId: {}", caseId);
        log.debug("Start event id: {}", startEventDetails.getEventId());
        CaseDetails<AsylumCase> caseDetails = startEventDetails.getCaseDetails();
        if (caseDetails == null) {
            log.info("Case details is null for caseId: {}", caseId);
            throw new IllegalStateException("Case details is null for caseId: " + caseId);
        } else {
            log.debug("Start case details id: {}", caseDetails.getId());
            log.debug("Start case details state: {}", caseDetails.getState());
            log.debug("Start case details created date: {}", caseDetails.getCreatedDate());
            AsylumCase asylumCase = caseDetails.getCaseData();
            log.debug("Start case details data: {}", asylumCase);

            Optional<StatutoryTimeframe24Weeks> existingData = asylumCase.read(STATUTORY_TIMEFRAME_24_WEEKS);
            String newHistoryId = nextHistoryId(existingData);

            checkStatusNotAlreadySet(newHistoryId, existingData, caseId);

            Map<String, Object> eventData = new HashMap<>();
            // Ugly hack to work around a CCD bug where event data properties with collection values are not being written to the case record
            // Reinstate this code if the CCD bug is ever fixed (yeah, I know) and make the corresponding change in ia-case-api
            // eventData.put(STF_24W_HOME_OFFICE_COHORT.value(), 
            //               hoStatutoryTimeframeDto.getStf24weekCohorts().stream()
            //               .filter(cohort -> cohort.isIncluded())
            //               .map(cohort -> cohort.getName()).collect(Collectors.joining(",")));
            // Remove this code if the CCD bug is ever fixed and make the corresponding change in ia-case-api
            eventData.put(STF_24W_HOME_OFFICE_COHORT.value(), 
                          hoStatutoryTimeframeDto.getStf24weekCohorts().stream()
                          .map(cohort -> cohort.getName() + "=" + cohort.getIncluded()).collect(Collectors.joining(",")));
            YesOrNo status = isYes ? YesOrNo.YES : YesOrNo.NO;
            eventData.put(STF_24W_CURRENT_STATUS_AUTO_GENERATED.value(), status);
            eventData.put(STF_24W_PREVIOUS_STATUS_WAS_YES_AUTO_GENERATED.value(), status);
            eventData.put(STF_24W_CURRENT_REASON_AUTO_GENERATED.value(), STATUTORY_TIMEFRAME_REASON);
            StatutoryTimeframe24Weeks stf24w = toStf24w(newHistoryId, status, hoStatutoryTimeframeDto);
            eventData.put(STATUTORY_TIMEFRAME_24_WEEKS.value(), stf24w);
            //eventData.put(STATUTORY_TIMEFRAME_24_WEEKS.value(), toStf24w(newHistoryId, status, hoStatutoryTimeframeDto));
            log.debug("Event data to be submitted: {}", eventData);

            SubmitEventDetails submitEventDetails = submitEvent(userToken, s2sToken, caseId, eventData, startEventDetails.getToken(), eventId, true);

            log.info("Home Office statutory timeframe status updated for the caseId: {}, Status: {}, Message: {}", caseId,
                     submitEventDetails.getCallbackResponseStatusCode(), submitEventDetails.getCallbackResponseStatus());

            return submitEventDetails;
        }
    }

    private String getCaseIdFromHmctsRefNum(String hmctsRefNum) {
        return dbUtils.getCaseId(hmctsRefNum);
    }

    private StartEventDetails getStartEventByCase(
        String userToken, String s2sToken, String caseId, String eventId) {
        log.debug("Getting start event by case with caseId: {}, EventId: {}", caseId, eventId);
        try {
            return ccdDataApi.startEventByCase(userToken, s2sToken, caseId, eventId);
        } catch (Exception ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("Case ID is not valid")) {
                log.info("Case not found for caseId: {}", caseId);
                throw new CaseNotFoundException("Case not found for caseId: " + caseId);
            }
            throw ex;
        }
    }

    private SubmitEventDetails submitEvent(
        String userToken, String s2sToken, String caseId, Map<String, Object> eventData,
        String eventToken, String eventId, boolean ignoreWarning) {

        log.debug("Event data to be submitted: {}", eventData);
        
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
        
        log.debug("Submitting case with caseId: {}, eventData: {}, ignoreWarning: {}",
                 caseId, eventData, ignoreWarning);
        
        return ccdDataApi.submitEventByCase(userToken, s2sToken, caseId, requestBody);
    }

    public StatutoryTimeframe24Weeks toStf24w(String historyId, YesOrNo status, HomeOfficeStatutoryTimeframeDto hoStatutoryTimeframeDto) {
        
        String dateTimeAdded = hoStatutoryTimeframeDto.getTimeStamp().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        
        StatutoryTimeframe24WeeksHistory historyEntry = new StatutoryTimeframe24WeeksHistory(
            status,
            STATUTORY_TIMEFRAME_REASON,
            STATUTORY_TIMEFRAME_USER,
            dateTimeAdded
        );

        List<IdValue<StatutoryTimeframe24WeeksHistory>> historyList = new ArrayList<>();
        historyList.add(new IdValue<>(historyId, historyEntry));

        log.debug("new StatutoryTimeframe24Weeks.history created with status: {}, date-time added: {} and history size: {}",
                 status, dateTimeAdded, historyList.size());
        log.debug("new StatutoryTimeframe24Weeks.homeOfficeResponse created: {}",
                 hoStatutoryTimeframeDto);

        return new StatutoryTimeframe24Weeks(
            historyList,
            hoStatutoryTimeframeDto
        );
        
    }

    public String nextHistoryId(Optional<StatutoryTimeframe24Weeks> existingData) {
        if (existingData.isEmpty()) {
            log.debug("No existing statutory timeframe 24 weeks data found, returning historyId: 1");
            return "1";
        }
        
        List<IdValue<StatutoryTimeframe24WeeksHistory>> existingHistory =
            existingData.get().getHistory();
        
        if (existingHistory == null || existingHistory.isEmpty()) {
            log.debug("Existing statutory timeframe 24 weeks data has no history, returning historyId: 1");
            return "1";
        }
        
        int maxId = existingHistory.stream()
            .map(IdValue::getId)
            .mapToInt(Integer::parseInt)
            .max()
            .orElse(0);
        
        String nextId = String.valueOf(maxId + 1);
        log.debug("Found {} existing history entries, max ID: {}, returning next historyId: {}",
                 existingHistory.size(), maxId, nextId);
        
        return nextId;
    }

    private void checkStatusNotAlreadySet(
        String newHistoryId,
        Optional<StatutoryTimeframe24Weeks> existingData,
        String caseId) {
        
        if (!newHistoryId.equals("1")) {
            String errorMessage = String.format(
                "Statutory timeframe status has already been set for caseId: %s",
                caseId
            );
            log.info(errorMessage);
            throw new IllegalStateException(errorMessage);
        }
    }

    public String generateS2SToken() {
        log.debug("Generating S2S token");
        String s2sToken = serviceAuthorization.generate();
        log.debug("S2S token generated successfully");
        return s2sToken;
    }

    public String getServiceUserToken() {
        log.debug("Generating service user token");
        String serviceUserToken = idamService.getServiceUserToken();
        log.debug("Service user token generated successfully");
        return serviceUserToken;
    }
}
