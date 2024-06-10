package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client;

import static feign.Request.create;
import static feign.Response.Body;
import static feign.Response.builder;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Request;
import feign.Request.HttpMethod;
import feign.Response;
import feign.RetryableException;
import feign.Util;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import org.assertj.core.api.Assertions;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
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

        try {
            feignErrorDecoder.decode("someMethod", response);
            fail("Expected a RetryableException to be thrown");
        } catch (RetryableException e) {
            Assertions.assertThat(e).isInstanceOf(RetryableException.class);
        }
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

        MatcherAssert.assertThat(feignErrorDecoder.decode("someMethod", response), instanceOf(HomeOfficeResponseException.class));
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

        MatcherAssert.assertThat(feignErrorDecoder.decode("someMethod", response),
                instanceOf(ResponseStatusException.class));
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

        MatcherAssert.assertThat(feignErrorDecoder.decode("someMethod", response),
                instanceOf(HomeOfficeResponseException.class));
    }

    @Test
    void handle_sneaky_exception() throws IOException {

        Body body = mock(Body.class);
        when(body.asReader(StandardCharsets.UTF_8))
            .thenThrow(new IOException("Error in reading response body"));

        response = builder()
            .status(400)
            .reason("Bad request")
            .request(create(HttpMethod.GET, "/api", Collections.emptyMap(),
                    Request.Body.empty(), null))
            .body(body)
            .build();

        MatcherAssert.assertThat(feignErrorDecoder.decode("someMethod", response),
                instanceOf(HomeOfficeResponseException.class));
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
        MatcherAssert.assertThat(exception, instanceOf(HomeOfficeResponseException.class));
        Assertions.assertThat(exception.getMessage().contains(
            "Invalid reference format. "
                + "Format should be either nnnn-nnnn-nnnn-nnnn or 0(0) followed by digits (total length 9 or 10)"
        )).isTrue();

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
}
