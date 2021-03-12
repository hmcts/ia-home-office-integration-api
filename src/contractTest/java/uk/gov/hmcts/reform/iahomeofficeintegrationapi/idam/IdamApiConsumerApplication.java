package uk.gov.hmcts.reform.iahomeofficeintegrationapi.idam;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.IdamApi;

@SpringBootApplication
@EnableFeignClients(clients = {
    IdamApi.class
})
public class IdamApiConsumerApplication {
    @MockBean
    RestTemplate restTemplate;
}
