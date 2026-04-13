package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import feign.Request;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeErrorResponse;

@Slf4j
public class HomeOfficeMissingApplicationDecoder implements ErrorDecoder {

    private final ObjectMapper objectMapper;

    public HomeOfficeMissingApplicationDecoder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        int statusCode = response.status();
        String responseBody;
        try {
            responseBody = IOUtils.toString(response.body().asReader(Charset.defaultCharset()));
        } catch (IOException ex) {
            responseBody = "[Response body could not be retrieved:\n\n" + ex.getMessage() + "]";
        }
        Request request = response.request();
        String requestUrl = request.url();
        var requestMethod = request.httpMethod().toString();
        Map<String, Collection<String>> requestHeaders = request.headers();
        String headersAsString = requestHeaders.entrySet().stream()
                                .flatMap(entry -> entry.getValue().stream()
                                .map(value -> "  " + entry.getKey() + ": " + value))
                                .collect(Collectors.joining(System.lineSeparator()));
        log.warn(
            "An exception was thrown from {}:\n\nHTTP {} request to Home Office endpoint {}\n\nHTTP headers:\n{}\nHTTP status code: {}\nHTTP response body:\n  {}",
            methodKey,
            requestMethod,
            requestUrl,
            headersAsString,
            statusCode,
            responseBody
        );
        // Prune the Home Office ID from the end of the URL
        String homeOfficeReferenceNumber = requestUrl.substring(requestUrl.lastIndexOf('/') + 1);
        // Throw new exception to be caught by the event handler
        String message = "Biographic information from Home Office asylum (etc.) application with HMCTS reference " + homeOfficeReferenceNumber + " could not be retrieved.";
        switch (statusCode) {
            case -1:
                message += "\n\nThe Home Office validation API did not respond.";
                break;
            case 400:
                message += "\n\nThe request to the Home Office validation API was not correctly formed.";
                break;
            case 401:
                message += "\n\nThe request to the Home Office validation API could not be authenticated.";
                break;
            case 403:
                message += "\n\nThe request to the Home Office validation API was authenticated but not authorised.";
                break;
            case 404:
                message += "\n\nNo application matching this HMCTS reference number was found.";
                break;
            case 500, 501, 502, 503, 504:
                message += "\n\nThe Home Office validation API was not available.";
                break;            
            default:
                message += "\n\nThe HTTP status code was " + statusCode + ".";
                break;
        }
        // Finally, add any specific details from the Home Office if they exist
        if (!responseBody.startsWith("[Response body could not be retrieved")) {
            try {
                HomeOfficeErrorResponse homeOfficeError = objectMapper.readValue(responseBody,HomeOfficeErrorResponse.class);
                if (homeOfficeError != null && homeOfficeError.getErrorDetail() != null) {
                    message += "\n\n" + "Home Office details:\n  Error code: %s\n  Message: %s".formatted(
                        homeOfficeError.getErrorDetail().getErrorCode(), homeOfficeError.getErrorDetail().getMessageText());
                }
            } catch (Exception ex) {
                // Log this
                log.warn("Could not read the Home Office error response:\n\n{}.", ex.getMessage());
            }
        }
        return new HomeOfficeMissingApplicationException(statusCode, message);
    }

}
