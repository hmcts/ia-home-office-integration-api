package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.service;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.CaseDataContent;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.StartEventDetails;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.SubmitEventDetails;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.CcdDataApi;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security.SystemTokenGenerator;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security.SystemUserProvider;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security.idam.IdentityManagerResponseException;

@Service
@Slf4j
public class CcdDataService {

    private final CcdDataApi ccdDataApi;
    private final SystemTokenGenerator systemTokenGenerator;
    private final SystemUserProvider systemUserProvider;
    private final AuthTokenGenerator serviceAuthorization;
    private static final String CASE_TYPE = "Asylum";

    public CcdDataService(CcdDataApi ccdDataApi,
                          SystemTokenGenerator systemTokenGenerator,
                          SystemUserProvider systemUserProvider,
                          AuthTokenGenerator serviceAuthorization) {

        this.ccdDataApi = ccdDataApi;
        this.systemTokenGenerator = systemTokenGenerator;
        this.systemUserProvider = systemUserProvider;
        this.serviceAuthorization = serviceAuthorization;
    }

    public SubmitEventDetails updateHomeOfficeFasterCaseStatus(Callback<AsylumCase> callback) {

        CaseDetails<AsylumCase> caseDetails = callback.getCaseDetails();
        String event = Event.UPDATE_HOME_OFFICE_FASTER_CASE_STATUS.toString();
        String caseId = String.valueOf(caseDetails.getId());
        String jurisdiction = caseDetails.getJurisdiction();

        String userToken;
        String s2sToken;
        String uid;
        try {
            userToken = "Bearer " + systemTokenGenerator.generate();
            log.info("System user token has been generated for event: {}, caseId: {}.", event, caseId);

            s2sToken = serviceAuthorization.generate();
            log.info("S2S token has been generated for event: {}, caseId: {}.", event, caseId);

            uid = systemUserProvider.getSystemUserId(userToken);
            log.info("System user id has been fetched for event: {}, caseId: {}.", event, caseId);

        } catch (IdentityManagerResponseException ex) {

            log.error("Unauthorised access to getCaseById", ex.getMessage());
            throw new IdentityManagerResponseException(ex.getMessage(), ex);
        }

        // Get case details by Id
        final StartEventDetails startEventDetails = getCase(userToken, s2sToken, uid, jurisdiction, CASE_TYPE, caseId);
        log.info("Case details found for the caseId: {}", caseId);

        // Assign value from API request body
        Map<String, Object> caseData = new HashMap<>();
        // GOT TO HERE ...

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("id", Event.UPDATE_HOME_OFFICE_FASTER_CASE_STATUS.toString());

        SubmitEventDetails submitEventDetails = submitEvent(userToken, s2sToken, caseId, caseData, eventData,
                                                            startEventDetails.getToken(), true);

        log.info("Legal Rep Details cleared for the caseId: {}, Status: {}, Message: {}", caseId,
                 submitEventDetails.getCallbackResponseStatusCode(), submitEventDetails.getCallbackResponseStatus());

        return submitEventDetails;
    }

    private StartEventDetails getCase(
        String userToken, String s2sToken, String uid, String jurisdiction, String caseType, String caseId) {

        return ccdDataApi.startEvent(userToken, s2sToken, uid, jurisdiction, caseType,
                                     caseId, Event.UPDATE_HOME_OFFICE_FASTER_CASE_STATUS.toString());
    }

    private SubmitEventDetails submitEvent(
        String userToken, String s2sToken, String caseId, Map<String, Object> data,
        Map<String, Object> eventData, String eventToken, boolean ignoreWarning) {

        CaseDataContent request =
            new CaseDataContent(caseId, data, eventData, eventToken, ignoreWarning);

        return ccdDataApi.submitEvent(userToken, s2sToken, caseId, request);
    }
}
