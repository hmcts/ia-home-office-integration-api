package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.CaseDataContent;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.StartEventDetails;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.SubmitEventDetails;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.config.FeignConfiguration;

@FeignClient(
    name = "core-case-data-api",
    url = "${core_case_data_api_url}",
    configuration = FeignConfiguration.class
)
public interface CcdDataApi {
    String EXPERIMENTAL = "experimental=true";
    String CONTENT_TYPE = "content-type=application/json";
    String SERVICE_AUTHORIZATION = "ServiceAuthorization";


    @GetMapping(
        path = "/case-types/{caseTypeId}/event-triggers/{triggerId}",
        headers = EXPERIMENTAL
    )
    StartEventDetails startEventByCase(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @PathVariable("caseTypeId") String caseType,
        @PathVariable("triggerId") String eventId
    );

    @PostMapping(
        value = "/cases/{cid}/events",
        headers = { CONTENT_TYPE, EXPERIMENTAL })
    SubmitEventDetails submitEvent(
        @RequestHeader(AUTHORIZATION) String userToken,
        @RequestHeader(SERVICE_AUTHORIZATION) String s2sToken,
        @PathVariable("cid") String id,
        @RequestBody CaseDataContent requestBody
    );


    @GetMapping(
        value = "/caseworkers/{uid}/jurisdictions/{jid}/case-types/{ctid}/cases/{cid}/event-triggers/{etid}"
            + "/token?ignore-warning=true",
        headers = CONTENT_TYPE
    )
    StartEventDetails startEvent(
        @RequestHeader(AUTHORIZATION) String userToken,
        @RequestHeader(SERVICE_AUTHORIZATION) String s2sToken,
        @PathVariable("uid") String userId,
        @PathVariable("jid") String jurisdiction,
        @PathVariable("ctid") String caseType,
        @PathVariable("cid") String cid,
        @PathVariable("etid") String eventId
    );

    @RequestMapping(
            method = RequestMethod.POST,
            value = "/caseworkers/{uid}/jurisdictions/{jid}/case-types/{ctid}/cases/{cid}/events"
    )
    CaseDetails submitEventForCaseWorker(
            @RequestHeader(AUTHORIZATION) String authorisation,
            @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
            @PathVariable("uid") String userId,
            @PathVariable("jid") String jurisdictionId,
            @PathVariable("ctid") String caseType,
            @PathVariable("cid") String caseId,
            @RequestParam("ignore-warning") boolean ignoreWarning,
            @RequestBody CaseDataContent caseDataContent
    );

}
