package uk.gov.hmcts.reform.iahomeofficeintegrationapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = {
    "uk.gov.hmcts.reform.authorisation",
    "uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client",
    "uk.gov.hmcts.reform.ccd.client"
})

@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor: it's not a utility class
public class Application {

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

