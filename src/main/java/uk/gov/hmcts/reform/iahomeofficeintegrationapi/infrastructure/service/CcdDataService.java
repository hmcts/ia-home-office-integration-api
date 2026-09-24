package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.service;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.CaseGoneException;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.CaseIncompatibleException;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.CaseNotFoundException;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeStatutoryTimeframe;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeStatutoryTimeframeDto;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeStatutoryTimeframeDto.Stf24WeekCohortDto;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.CaseDataContent;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.StartEventDetails;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.StatutoryTimeframe24Weeks;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.StatutoryTimeframe24WeeksHistory;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.SubmitEventDetails;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.IdamService;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.DbUtils;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.CcdDataApi;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security.idam.IdentityManagerResponseException;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.STATUTORY_TIMEFRAME_24_WEEKS;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.STF_24W_CURRENT_REASON_AUTO_GENERATED;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.STF_24W_CURRENT_STATUS_AUTO_GENERATED;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.STF_24W_HOME_OFFICE_COHORT;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.STF_24W_PREVIOUS_STATUS_WAS_YES_AUTO_GENERATED;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event.STF_24W_DETERMINATION;

@Service
@Slf4j
public class CcdDataService {

    private static final String STATUTORY_TIMEFRAME_REASON = "Home Office initial determination";
    private static final String STATUTORY_TIMEFRAME_USER = "Home Office Integration API";

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
        boolean isYes = hoStatutoryTimeframeDto.getStf24weekCohortDtos().stream().anyMatch(Stf24WeekCohortDto::isIncluded);
        String caseId = getCaseIdFromHmctsRefNum(hoStatutoryTimeframeDto.getHmctsReferenceNumber());

        String userToken;
        String s2sToken;
        try {
            userToken = normaliseBearerToken(idamService.getServiceUserToken());
            s2sToken = normaliseBearerToken(serviceAuthorization.generate());
        } catch (IdentityManagerResponseException ex) {
            log.info("Unauthorised access to getCaseById: {}", ex.getMessage());
            throw new IdentityManagerResponseException(ex.getMessage(), ex);
        }

        final StartEventDetails startEventDetails;
        try {
            startEventDetails = getStartEventByCase(userToken, s2sToken, caseId, isYes);
        } catch (FeignException.NotFound ex) {
            String message = "Case no longer exists for case ID " + caseId + ".";
            log.warn("{}\n\n{}", message, ex.getMessage());
            // This exception will result in a 410 (Gone) code being returned to the Home Office.  This is
            // more descriptive than a 404, which is what CaseNotFoundException() would cause to be returned.
            throw new CaseGoneException(message);
        }
        CaseDetails<AsylumCase> caseDetails = startEventDetails.getCaseDetails();
        if (caseDetails == null) {
            throw new IllegalStateException("Case details is null for caseId: " + caseId);
        }

        AsylumCase asylumCase = caseDetails.getCaseData();
        StatutoryTimeframe24Weeks existingData = asylumCase.read(STATUTORY_TIMEFRAME_24_WEEKS, StatutoryTimeframe24Weeks.class).orElse(null);
        String newHistoryId = nextHistoryId(existingData);

        checkStatusNotAlreadySet(existingData, caseId);

        Map<String, Object> eventData = new HashMap<>();
        eventData.put(STF_24W_HOME_OFFICE_COHORT.value(),
            hoStatutoryTimeframeDto.getStf24weekCohortDtos().stream()
                .filter(Stf24WeekCohortDto::isIncluded)
                .map(Stf24WeekCohortDto::getName).collect(Collectors.joining(",")));
        YesOrNo status = isYes ? YesOrNo.YES : YesOrNo.NO;
        eventData.put(STF_24W_CURRENT_STATUS_AUTO_GENERATED.value(), status);
        eventData.put(STF_24W_PREVIOUS_STATUS_WAS_YES_AUTO_GENERATED.value(), status);
        eventData.put(STF_24W_CURRENT_REASON_AUTO_GENERATED.value(), STATUTORY_TIMEFRAME_REASON);
        StatutoryTimeframe24Weeks stf24w = toStf24w(newHistoryId, status, hoStatutoryTimeframeDto);
        eventData.put(STATUTORY_TIMEFRAME_24_WEEKS.value(), stf24w);

        String summary = "Home Office statutory timeframe status determined as " + (isYes ? "not " : "") + "suitable for 24 week timeframe.";

        return submitEvent(userToken, s2sToken, caseId, eventData, startEventDetails.getToken(), summary);

    }

    private String getCaseIdFromHmctsRefNum(String hmctsRefNum) {
        try {
            return dbUtils.getCaseId(hmctsRefNum);
        } catch (IllegalStateException ex) {
            String message = ex.getMessage();
            log.warn(message);
            // Change the type of exception to ensure that a 404 is returned to the Home Office here rather than a 409.
            throw new CaseNotFoundException(message);
        }
    }

    private StartEventDetails getStartEventByCase(String userToken, String s2sToken, String caseId, boolean isYes) {
        try {
            return ccdDataApi.startEventByCase(userToken, s2sToken, caseId, STF_24W_DETERMINATION.toString());
        } catch (Exception ex) {
            String exMessage = ex.getMessage();
            if (exMessage != null && exMessage.contains("Case ID is not valid")) {
                String message = "Case no longer exists for case ID " + caseId + ".";
                log.warn("{}\n\n{}", message, exMessage);
                // This exception will result in a 410 (Gone) code being returned to the Home Office.  This is
                // more descriptive than a 404, which is what CaseNotFoundException() would cause to be returned.
                throw new CaseGoneException(message);
            } else if (exMessage != null && exMessage.contains("\"status\":422,\"error\":\"Unprocessable Entity\"")) {
                String message = "Case incompatible with supplied 24-week status for case ID " + caseId + ".";
                log.warn("{}\n\n{}", message, exMessage);
                // This exception will result in a 422 (Unprocessable Entity) code being returned to the Home Office.  This is
                // more descriptive than a 409, which at the moment we are using solely to indicate that the status has already been set.
                throw new CaseIncompatibleException(message, isYes ? YesOrNo.YES : YesOrNo.NO);
            }
            throw ex;
        }
    }

    private SubmitEventDetails submitEvent(
        String userToken, String s2sToken, String caseId, Map<String, Object> eventData,
        String eventToken, String summary) {

        Map<String, Object> eventMetadata = new HashMap<>();
        eventMetadata.put("id", STF_24W_DETERMINATION.toString());
        eventMetadata.put("summary", summary);
        eventMetadata.put("description", "");

        CaseDataContent requestBody =
            new CaseDataContent(caseId, eventData, eventMetadata, eventToken, false);

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

        return new StatutoryTimeframe24Weeks(
            historyList,
            new HomeOfficeStatutoryTimeframe(hoStatutoryTimeframeDto)
        );

    }

    public String nextHistoryId(StatutoryTimeframe24Weeks existingData) {
        if (existingData == null) {
            log.debug("No existing statutory timeframe 24 weeks data found, returning historyId: 1");
            return "1";
        }

        List<IdValue<StatutoryTimeframe24WeeksHistory>> existingHistory =
            existingData.getHistory();

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
        StatutoryTimeframe24Weeks existingData,
        String caseId) {

        Optional.ofNullable(existingData)
            .map(StatutoryTimeframe24Weeks::getHistory)
            .filter(history -> !history.isEmpty())
            .ifPresent(history -> {
                String errorMessage = "Statutory timeframe status has already been set for caseId: %s".formatted(caseId);
                log.info(errorMessage);
                throw new IllegalStateException(errorMessage);
            });
    }

    private String normaliseBearerToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("Token is null or blank");
        }
        String stripped = token.trim().replaceAll("(?i)^(Bearer\\s+)+", "");
        return "Bearer " + stripped;
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
