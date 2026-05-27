package uk.gov.hmcts.reform.iahomeofficeintegrationapi.idam;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.IdamApi;

@SpringBootApplication(
    exclude = {
        DataSourceAutoConfiguration.class
    }
)
@EnableFeignClients(clients = {
    IdamApi.class
})
public class IdamApiConsumerApplication {

    @MockitoBean
    RestTemplate restTemplate;
}