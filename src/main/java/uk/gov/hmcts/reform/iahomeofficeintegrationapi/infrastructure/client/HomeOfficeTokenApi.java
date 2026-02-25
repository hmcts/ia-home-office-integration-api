package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client;

import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(name = "home-office-token-api", url = "${home-office.api.url}", configuration = FeignErrorDecoderConfig.class)
public interface HomeOfficeTokenApi {

    @PostMapping(value = "/ichallenge/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    String getAuthorizationToken(
        @RequestBody Map<String, ?> request);
}
