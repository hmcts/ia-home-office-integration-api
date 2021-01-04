package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeInstruct;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeInstructResponse;

@FeignClient(name = "home-office-instruct-api", url = "${home-office.api.url}")
public interface HomeOfficeInstructApi {

    @PostMapping("/ichallenge/applicationInstruct/setInstruct")
    HomeOfficeInstructResponse sendNotification(
        @RequestHeader(AUTHORIZATION) String bearerToken,
        @RequestBody HomeOfficeInstruct request
    );
}
