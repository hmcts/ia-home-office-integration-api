package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Request;
import feign.Response;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeError;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeErrorResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.MessageHeader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HomeOfficeMissingApplicationDecoderTest {

    private ObjectMapper objectMapper;
    private HomeOfficeMissingApplicationDecoder decoder;

    @BeforeEach
    void setUp() {
        objectMapper = mock(ObjectMapper.class);
        decoder = new HomeOfficeMissingApplicationDecoder(objectMapper);
    }

    private Response buildResponse(int status, String body) {
        Request request = Request.create(
            Request.HttpMethod.GET,
            "http://localhost/home-office/123456",
            Collections.emptyMap(),
            null,
            StandardCharsets.UTF_8,
            null
        );

        return Response.builder()
            .status(status)
            .reason("reason")
            .request(request)
            .headers(Collections.emptyMap())
            .body(body, StandardCharsets.UTF_8)
            .build();
    }

    @Test
    void shouldHandle404WithHomeOfficeDetails() throws Exception {
        String json = "{\"errorDetail\":{\"errorCode\":\"E001\",\"messageText\":\"Not Found\"}}";

        HomeOfficeError homeOfficeError = mock(HomeOfficeError.class);
        when(homeOfficeError.getErrorCode()).thenReturn("E001");
        when(homeOfficeError.getMessageText()).thenReturn("Not Found");

        HomeOfficeErrorResponse errorResponse =
            new HomeOfficeErrorResponse(mock(MessageHeader.class), homeOfficeError);

        when(objectMapper.readValue(json, HomeOfficeErrorResponse.class))
            .thenReturn(errorResponse);

        Response response = buildResponse(404, json);

        Exception exception = decoder.decode("methodKey", response);

        assertThat(exception).isInstanceOf(HomeOfficeMissingApplicationException.class);
        assertThat(exception.getMessage()).contains("No application matching this HMCTS reference number was found.");
        assertThat(exception.getMessage()).contains("E001");
        assertThat(exception.getMessage()).contains("Not Found");
    }

    @Test
    void shouldNotAppendDetailsWhenErrorDetailIsNull() throws Exception {
        String json = "{}";

        HomeOfficeErrorResponse errorResponse =
            new HomeOfficeErrorResponse(mock(MessageHeader.class), null);

        when(objectMapper.readValue(json, HomeOfficeErrorResponse.class))
            .thenReturn(errorResponse);

        Response response = buildResponse(404, json);

        Exception exception = decoder.decode("methodKey", response);

        assertThat(exception.getMessage())
            .doesNotContain("Home Office details:");
    }

    @Test
    void shouldIgnoreJsonParsingException() throws Exception {
        String invalidJson = "invalid";

        when(objectMapper.readValue(invalidJson, HomeOfficeErrorResponse.class))
            .thenThrow(new RuntimeException("parse error"));

        Response response = buildResponse(404, invalidJson);

        Exception exception = decoder.decode("methodKey", response);

        assertThat(exception.getMessage())
            .contains("No application matching this HMCTS reference number was found.");
    }

    @Test
    void shouldHandle400BadRequest() {
        Response response = buildResponse(400, "{}");

        Exception exception = decoder.decode("methodKey", response);

        assertThat(exception.getMessage())
            .contains("was not correctly formed.");
    }

    @Test
    void shouldHandle401Unauthorized() {
        Response response = buildResponse(401, "{}");

        Exception exception = decoder.decode("methodKey", response);

        assertThat(exception.getMessage())
            .contains("could not be authenticated.");
    }

    @Test
    void shouldHandle403Forbidden() {
        Response response = buildResponse(403, "{}");

        Exception exception = decoder.decode("methodKey", response);

        assertThat(exception.getMessage())
            .contains("authenticated but not authorised.");
    }

    @Test
    void shouldHandleServerErrors() {
        Response response = buildResponse(503, "{}");

        Exception exception = decoder.decode("methodKey", response);

        assertThat(exception.getMessage())
            .contains("validation API was not available.");
    }

    @Test
    void shouldHandleStatusMinusOne() {
        Response response = buildResponse(-1, "{}");

        Exception exception = decoder.decode("methodKey", response);

        assertThat(exception.getMessage())
            .contains("did not respond.");
    }

    @Test
    void shouldHandleUnknownStatusCode() {
        Response response = buildResponse(418, "{}");

        Exception exception = decoder.decode("methodKey", response);

        assertThat(exception.getMessage())
            .contains("The HTTP status code was 418.");
    }

    @Test
    void shouldHandleInputOutputExceptionWhenReadingResponseBody() throws IOException {
        Request request = Request.create(
            Request.HttpMethod.GET,
            "http://localhost/home-office/123456",
            Collections.emptyMap(),
            null,
            StandardCharsets.UTF_8,
            null
        );

        Response.Body body = mock(Response.Body.class);
        when(body.asReader(StandardCharsets.UTF_8))
            .thenThrow(new IOException("boom"));

        Response response = Response.builder()
            .status(404)
            .reason("reason")
            .request(request)
            .headers(Collections.emptyMap())
            .body(body)
            .build();

        Exception exception = decoder.decode("methodKey", response);

        assertThat(exception.getMessage())
            .contains("No application matching this HMCTS reference number was found.");
    }
}