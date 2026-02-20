package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeApplicationDto;

@FeignClient(name = "home-office-application-api", url = "${home-office.api.url}") // http://localhost:8081 for testing
public interface HomeOfficeApplicationApi {

    @GetMapping("/applications/v1/{id}")
    ResponseEntity<HomeOfficeApplicationDto> getApplication(@PathVariable(name = "id") final String uanOrGwf,
        @RequestHeader(AUTHORIZATION) String bearerToken,
        @RequestHeader("Home-Office-Correlation-ID") final String correlationID,
        @RequestHeader("Home-Office-Consumer") final String consumerCode,
        @RequestHeader("Home-Office-Event-DateTime") final String eventDateTime);
}
