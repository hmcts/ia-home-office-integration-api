package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.service;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeStatutoryTimeframeDto;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.CaseDataContent;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.StartEventDetails;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.StatutoryTimeFrame24WeeksFieldValue;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.SubmitEventDetails;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.CcdDataApi;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security.idam.IdentityManagerResponseException;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.IdamService;

import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.STATUTORY_TIMEFRAME_24WEEKS;


@Service
@Slf4j
public class CcdDataService {

    private final CcdDataApi ccdDataApi;
    private final IdamService idamService;
    private final AuthTokenGenerator serviceAuthorization;

    private static final String JURISDICTION = "IA";
    private static final String CASE_TYPE = "Asylum";

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

        String event = Event.SET_HOME_OFFICE_STATUTORY_TIMEFRAME_STATUS.toString();
        String caseId = hoStatutoryTimeframeDto.getCcdCaseNumber();

        String userToken;
        String s2sToken;
        String userId;
        try {
            userToken = "Bearer " + idamService.getServiceUserToken();
            log.info("System user token has been generated for event: {}, caseId: {}.", event, caseId);

            s2sToken = serviceAuthorization.generate();
            log.info("S2S token has been generated for event: {}, caseId: {}.", event, caseId);

            userId = idamService.getUserInfo(userToken).getUid();
            log.info("System user id has been fetched for event: {}, caseId: {}.", event, caseId);

        } catch (IdentityManagerResponseException ex) {
            log.error("Unauthorised access to getCaseById", ex.getMessage());
            throw new IdentityManagerResponseException(ex.getMessage(), ex);
        }

        String eventId = Event.SET_HOME_OFFICE_STATUTORY_TIMEFRAME_STATUS.toString();
                
        log.info("ccd url: {}", coreCaseDataApiUrl);
        final StartEventDetails startEventDetails = getCase(userToken, s2sToken, userId, JURISDICTION, CASE_TYPE, caseId, eventId);
        log.info("Case details found for the caseId: {}", caseId);

        Map<String, Object> caseData = new HashMap<>();
        caseData.put(STATUTORY_TIMEFRAME_24WEEKS.value(), toStf4w("1", hoStatutoryTimeframeDto));

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("id", Event.SET_HOME_OFFICE_STATUTORY_TIMEFRAME_STATUS.toString());

        SubmitEventDetails submitEventDetails = submitEvent(userToken, s2sToken, caseId, caseData, eventData,
                                                            startEventDetails.getToken(), true);

        log.info("Home Office statutory timeframe status updated for the caseId: {}, Status: {}, Message: {}", caseId,
                 submitEventDetails.getCallbackResponseStatusCode(), submitEventDetails.getCallbackResponseStatus());

        return submitEventDetails;
    }

    private StartEventDetails getCase(
        String userToken, String s2sToken, String uid, String jurisdiction, String caseType, String caseId, String eventId) {
        //log all the arguments
        log.info("Getting case with userToken: {}, s2sToken: {}, uid: {}, jurisdiction: {}, caseType: {}, caseId: {}, EventId: {}",
                 userToken, s2sToken, uid, jurisdiction, caseType, caseId, eventId);
        return ccdDataApi.startEvent(userToken, s2sToken, uid, jurisdiction, caseType,
                                     caseId, eventId);

                                     
    }

    private SubmitEventDetails submitEvent(
        String userToken, String s2sToken, String caseId, Map<String, Object> caseData,
        Map<String, Object> eventData, String eventToken, boolean ignoreWarning) {

        CaseDataContent request =
            new CaseDataContent(caseId, caseData, eventData, eventToken, ignoreWarning);

        return ccdDataApi.submitEvent(userToken, s2sToken, caseId, request);
    }

    public List<IdValue<StatutoryTimeFrame24WeeksFieldValue>> toStf4w(String id, HomeOfficeStatutoryTimeframeDto hoStatutoryTimeframeDto) {
        
        YesOrNo status = hoStatutoryTimeframeDto.isHoStatutoryTimeframeStatus() ? YesOrNo.YES : YesOrNo.NO;
        String reason = "Home Office statutory timeframe update";
        String user = "Home Office Integration API";
        String dateTimeAdded = hoStatutoryTimeframeDto.getTimeStamp().format(DateTimeFormatter.ISO_LOCAL_DATE) + "T00:00:00Z";
        
        StatutoryTimeFrame24WeeksFieldValue statutoryTimeframeValue = new StatutoryTimeFrame24WeeksFieldValue(
            status,
            reason,
            user,
            dateTimeAdded
        );
        
        IdValue<StatutoryTimeFrame24WeeksFieldValue> idValue = new IdValue<>(id, statutoryTimeframeValue);
        
        return List.of(idValue);
    }
}
