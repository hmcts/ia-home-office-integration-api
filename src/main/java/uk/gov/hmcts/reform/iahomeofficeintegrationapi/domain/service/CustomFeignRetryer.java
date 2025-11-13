package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service;

import feign.RetryableException;
import feign.Retryer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.RetriesExceededException;

@Slf4j
@Service
public class CustomFeignRetryer {

    private final int retryMaxAttempt;

    private final long retryInterval;

    private int attempt = 1;

    public CustomFeignRetryer(
        @Value("${home-office.feign.retry.count}")  int numberOfRetries,
        @Value("${home-office.feign.retry.wait-in-millis}") long timeToWait) {

        this.retryMaxAttempt = numberOfRetries;
        this.retryInterval = timeToWait;
    }

    public static CustomFeignRetryer newInstance(CustomFeignRetryer customFeignRetryer) {
        return new CustomFeignRetryer(customFeignRetryer.retryMaxAttempt,
                customFeignRetryer.retryInterval);
    }
}
