package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service;

import feign.RetryableException;
import feign.Retryer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.RetriesExceededException;

@Slf4j
@Service
public class CustomFeignRetryer  implements Retryer {
    @Value("${home-office.feign.retry.count}")
    private int retryMaxAttempt;

    @Value("${home-office.feign.retry.wait-in-millis}")
    private long retryInterval;

    private int attempt = 1;

    public CustomFeignRetryer() {

        this.retryMaxAttempt = retryMaxAttempt;
        this.retryInterval = retryInterval;
    }

    private CustomFeignRetryer(CustomFeignRetryer other) {

        this.retryMaxAttempt = other.retryMaxAttempt;
        this.retryInterval = other.retryInterval;
        this.attempt = 1;
    }

    @Override
    public void continueOrPropagate(RetryableException e) {
        log.info("Feign retry attempt {} due to {} ", attempt, e.getMessage());

        if (attempt++ == retryMaxAttempt) {
            throw new RetriesExceededException("Retry Failed: Total " + (attempt - 1)
                    + " attempts made at interval " + retryInterval
                    + "ms", e);
        }
        try {
            Thread.sleep(retryInterval);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public Retryer clone() {
        return new CustomFeignRetryer(this);
    }
}
