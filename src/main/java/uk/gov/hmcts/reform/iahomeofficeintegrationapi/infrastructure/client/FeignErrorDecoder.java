package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import java.io.IOException;
import java.nio.charset.Charset;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeInstructResponse;

@Component
@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {

    private final String errorLog = "Error in reading response body %s";
    private ObjectMapper objectMapper;

    public FeignErrorDecoder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Exception decode(String methodKey, Response response) {

        switch (response.status()) {
            case 400:
                String errMessage = "";
                try {
                    if (response.body() != null && response.body().asInputStream() != null) {
                        HomeOfficeInstructResponse homeOfficeError = objectMapper.readValue(
                            response.body().asInputStream(),
                            HomeOfficeInstructResponse.class);
                        if (homeOfficeError != null) {
                            errMessage = String.format("Home office error code: %s, message: %s",
                                homeOfficeError.getErrorDetail().getErrorCode(),
                                homeOfficeError.getErrorDetail().getMessageText());
                        }
                    }

                    log.error("StatusCode: {}, methodKey: {}, reason: {}, message: {}",
                        response.status(),
                        methodKey,
                        response.reason(),
                        (!errMessage.equals("") ? errMessage :
                            IOUtils.toString(response.body().asReader(Charset.defaultCharset()))));

                } catch (IOException ex) {
                    errMessage = String.format(errorLog, ex.getMessage());
                    log.error(errorLog, ex.getMessage());
                }
                return new HomeOfficeResponseException(String.format(
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
                    log.error(errorLog, ex.getMessage());
                }
                return new ResponseStatusException(HttpStatus.valueOf(response.status()), response.reason());

            default:
                return new HomeOfficeResponseException(response.reason());

        }
    }
}
