package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;

import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeErrorResponse;

@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {

    private static final String ERROR_LOG = "Error in reading response body %s";
    private final ObjectMapper objectMapper;

    public FeignErrorDecoder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        if (methodKey.contains("RoleAssignmentApi")) {
            // Let the default error decoder handle it
            return new ErrorDecoder.Default().decode(methodKey, response);
        }
        switch (response.status()) {
            case 400:
                String errMessage = "";
                String errorCode = "";
                try {
                    if (response.body() != null && response.body().asInputStream() != null) {

                        String rawResponse = IOUtils.toString(response.body().asReader(Charset.defaultCharset()));
                        log.error("Raw 400 response from {}: {}", methodKey, rawResponse);
                        
                        HomeOfficeErrorResponse homeOfficeError = objectMapper.readValue(
                            rawResponse,HomeOfficeErrorResponse.class);

                        if (homeOfficeError != null) {
                            if (homeOfficeError.getErrorDetail() != null) {
                                errorCode = homeOfficeError.getErrorDetail().getErrorCode();
                                errMessage = String.format("Home office error code: %s, message: %s",
                                    errorCode, homeOfficeError.getErrorDetail().getMessageText());
                            } else {
                                errMessage = "Home office error detail is null";
                            }
                        }
                    }

                    log.error("Error StatusCode: {}, methodKey: {}, reason: {}, message: {}",
                        response.status(), methodKey, response.reason(), errMessage);

                } catch (IOException ex) {
                    errMessage = String.format(ERROR_LOG, ex.getMessage());
                    log.error(ERROR_LOG, ex.getMessage());
                }
                return new HomeOfficeResponseException(errorCode, String.format(
                    "StatusCode: %d, methodKey: %s, reason: %s, message: %s",
                    response.status(),
                    methodKey,
                    response.reason(),
                    errMessage));

            case 404:
                try {

                    log.error("StatusCode: {}, methodKey: {}, reason: {}, message: {}",
                        response.status(),
                        methodKey,
                        response.reason(),
                        IOUtils.toString(response.body().asReader(Charset.defaultCharset())));
                } catch (IOException ex) {
                    log.error(ERROR_LOG, ex.getMessage());
                }
                return new ResponseStatusException(HttpStatus.valueOf(response.status()), response.reason());

            case 500:
                log.error("StatusCode: {}, methodKey: {}, reason: {}",
                    response.status(),
                    methodKey,
                    response.reason());
                throw new RetryableException(response.status(), response.reason(), response.request().httpMethod(), null, response.request());

            default:
                log.error("StatusCode: {}, methodKey: {}, reason: {}",
                    response.status(),
                    methodKey,
                    response.reason()
                );
                return new HomeOfficeResponseException(response.reason());

        }
    }
}
