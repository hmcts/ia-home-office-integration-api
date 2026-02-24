package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeSearch;


@FeignClient(name = "home-office-case-search-api", url = "${home-office.api.url}", configuration = FeignErrorDecoderConfig.class)
public interface HomeOfficeSearchApi {

    @PostMapping("/ichallenge/applicationStatus/getBySearchParameters")
    String getStatus(
        @RequestHeader(AUTHORIZATION) String bearerToken,
        @RequestBody HomeOfficeSearch request
    );
}
