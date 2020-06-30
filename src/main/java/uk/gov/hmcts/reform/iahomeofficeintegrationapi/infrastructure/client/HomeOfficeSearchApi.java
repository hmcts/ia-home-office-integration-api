package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeSearch;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeSearchResponse;

@FeignClient(name = "home-office-case-search-api", url = "${home-office.api.url}")
public interface HomeOfficeSearchApi {

    @PostMapping("/v1/applicationStatus/getBySearchParameters")
    HomeOfficeSearchResponse getStatus(
        @RequestBody HomeOfficeSearch request
    );
}
