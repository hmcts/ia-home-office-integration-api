package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service;

import feign.RetryableException;
import feign.Retryer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.RetriesExceededException;

@Slf4j
public class CustomFeignRetryer  implements Retryer {

    private final int retryMaxAttempt;

    private final long retryInterval;

    private int attempt = 1;

    public CustomFeignRetryer(
            int numberOfRetries,
            long timeToWait) {

        this.retryMaxAttempt = numberOfRetries;
        this.retryInterval = timeToWait;
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
