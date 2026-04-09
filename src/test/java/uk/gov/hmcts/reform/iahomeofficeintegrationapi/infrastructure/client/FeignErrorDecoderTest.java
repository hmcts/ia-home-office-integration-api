package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client;

import static feign.Request.create;
import static feign.Response.Body;
import static feign.Response.builder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.Request;
import feign.Request.HttpMethod;
import feign.Response;
import feign.RetryableException;
import feign.Util;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class FeignErrorDecoderTest {

    @Mock
    private Response response;

    private FeignErrorDecoder feignErrorDecoder;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        feignErrorDecoder = new FeignErrorDecoder(objectMapper);
    }

    @Test
    void should_decode_for_500() {

        response = builder()
            .status(500)
            .reason("Internal server error")
            .request(create(HttpMethod.GET, "/api", Collections.emptyMap(),
                Request.Body.empty(), null))
            .body("Internal server error", Util.UTF_8)
            .build();

        assertThrows(RetryableException.class,
            () -> feignErrorDecoder.decode("someMethod", response));
    }

    @Test
    void should_decode_for_default() {

        response = builder()
            .status(403)
            .reason("Forbidden")
            .request(create(HttpMethod.GET, "/api", Collections.emptyMap(),
                    Request.Body.empty(), null))
            .body("Authorization failed", Util.UTF_8)
            .build();
        Exception exception = feignErrorDecoder.decode("someMethod", response);
        assertEquals("Forbidden", exception.getMessage());
        assertEquals(HomeOfficeResponseException.class, exception.getClass());
    }

    @Test
    void should_decode_for_404() {

        response = builder()
            .status(404)
            .reason("Not found")
            .request(create(HttpMethod.GET, "/api", Collections.emptyMap(),
                    Request.Body.empty(), null))
            .body("No data found", Util.UTF_8)
            .build();

        Exception exception = feignErrorDecoder.decode("someMethod", response);
        assertTrue(exception.getMessage().contains("404"));
        assertTrue(exception.getMessage().contains("Not found"));
        assertEquals(ResponseStatusException.class, exception.getClass());
    }

    @Test
    void should_decode_for_400() {

        response = builder()
            .status(400)
            .reason("Bad request")
            .request(create(HttpMethod.GET, "/api", Collections.emptyMap(),
                    Request.Body.empty(), null))
            .body("Bad request data".getBytes())
            .build();

        Exception exception = feignErrorDecoder.decode("someMethod", response);
        assertTrue(exception.getMessage().contains("400"));
        assertTrue(exception.getMessage().contains("Bad request"));
        assertEquals(HomeOfficeResponseException.class, exception.getClass());
    }

    @Test
    void handle_sneaky_exception() throws IOException {

        Body body = mock(Body.class);
        when(body.asReader(Charset.forName("UTF-8")))
            .thenThrow(new IOException("Error in reading response body"));

        response = builder()
            .status(400)
            .reason("Bad request")
            .request(create(HttpMethod.GET, "/api", Collections.emptyMap(),
                    Request.Body.empty(), null))
            .body(body)
            .build();

        Exception exception = feignErrorDecoder.decode("someMethod", response);
        assertTrue(exception.getMessage().contains("400"));
        assertTrue(exception.getMessage().contains("Bad request"));
        assertEquals(HomeOfficeResponseException.class, exception.getClass());
    }

    @Test
    void should_decode_for_400_home_office_error() {

        response = builder()
            .status(400)
            .reason("Bad request")
            .request(create(HttpMethod.GET, "/api", Collections.emptyMap(),
                    Request.Body.empty(), null))
            .body(getErrorDetail())
            .build();

        Exception exception = feignErrorDecoder.decode("someMethod", response);
        assertEquals(HomeOfficeResponseException.class, exception.getClass());
        assertTrue(exception.getMessage().contains(
            "Invalid reference format. "
                + "Format should be either nnnn-nnnn-nnnn-nnnn or 0(0) followed by digits (total length 9 or 10)"
        ));

    }

    private byte[] getErrorDetail() {
        String errorResponse = "{\n"
            + "  \"errorDetail\": {\n"
            + "    \"errorCode\": \"1100\",\n"
            + "    \"messageText\": \"Invalid reference format. "
            + "Format should be either nnnn-nnnn-nnnn-nnnn or 0(0) followed by digits (total length 9 or 10)\",\n"
            + "    \"success\": true\n"
            + "  }"
            + "}";

        return errorResponse.getBytes();
    }

    @Test
    void should_decode_for_404_ccd_api_case_not_found() {
        String ccdErrorResponse = "{\"exception\":\"uk.gov.hmcts.ccd.endpoint.exceptions.BadRequestException\","
            + "\"status\":400,\"error\":\"Bad Request\",\"message\":\"Case ID is not valid\","
            + "\"path\":\"/cases/123/event-triggers/addStatutoryTimeframe24Weeks\"}";

        response = builder()
            .status(400)
            .reason("Bad request")
            .request(create(HttpMethod.GET, "/api", Collections.emptyMap(),
                    Request.Body.empty(), null))
            .body(ccdErrorResponse.getBytes())
            .build();

        Exception exception = feignErrorDecoder.decode("CcdDataApi#startEventByCase", response);
        // CCD returns 400 "Case ID is not valid" when case not found - should be treated as 404
        assertEquals(ResponseStatusException.class, exception.getClass());
        ResponseStatusException responseStatusException = (ResponseStatusException) exception;
        assertEquals(HttpStatus.NOT_FOUND, responseStatusException.getStatus());
        assertTrue(exception.getMessage().contains("Case ID is not valid"));
    }

    @Test
    void should_decode_for_400_ccd_api_other_error() {
        String ccdErrorResponse = "{\"exception\":\"some.Exception\","
            + "\"status\":400,\"error\":\"Bad Request\",\"message\":\"Some other CCD error\"}";

        response = builder()
            .status(400)
            .reason("Bad request")
            .request(create(HttpMethod.GET, "/api", Collections.emptyMap(),
                    Request.Body.empty(), null))
            .body(ccdErrorResponse.getBytes())
            .build();

        Exception exception = feignErrorDecoder.decode("CcdDataApi#submitEvent", response);
        assertEquals(HomeOfficeResponseException.class, exception.getClass());
        assertTrue(exception.getMessage().contains("Some other CCD error"));
    }

    @ParameterizedTest
    @MethodSource("feignExceptionSource")
    void should_default_decode_for_role_assignment_api(int status, Class exceptionClass) {

        response = builder()
            .status(status)
            .reason("some error message")
            .request(create(HttpMethod.GET, "/api", Collections.emptyMap(),
                Request.Body.empty(), null))
            .body("Bad request data".getBytes())
            .build();

        Exception exception = feignErrorDecoder.decode("RoleAssignmentApi#somemethod", response);
        assertTrue(exception.getMessage().contains(String.valueOf(status)));
        assertTrue(exception.getMessage().contains("some error message"));
        assertEquals(exceptionClass, exception.getClass());
    }

    private static Stream<Arguments> feignExceptionSource() {
        return Stream.of(
            Arguments.of(400, FeignException.BadRequest.class),
            Arguments.of(401, FeignException.Unauthorized.class),
            Arguments.of(403, FeignException.Forbidden.class),
            Arguments.of(404, FeignException.NotFound.class),
            Arguments.of(405, FeignException.MethodNotAllowed.class),
            Arguments.of(406, FeignException.NotAcceptable.class),
            Arguments.of(409, FeignException.Conflict.class),
            Arguments.of(410, FeignException.Gone.class),
            Arguments.of(415, FeignException.UnsupportedMediaType.class),
            Arguments.of(422, FeignException.UnprocessableEntity.class),
            Arguments.of(429, FeignException.TooManyRequests.class),
            Arguments.of(402, FeignException.FeignClientException.class),
            Arguments.of(500, FeignException.InternalServerError.class),
            Arguments.of(501, FeignException.NotImplemented.class),
            Arguments.of(502, FeignException.BadGateway.class),
            Arguments.of(503, FeignException.ServiceUnavailable.class),
            Arguments.of(504, FeignException.GatewayTimeout.class),
            Arguments.of(599, FeignException.FeignServerException.class)
        );
    }
}
