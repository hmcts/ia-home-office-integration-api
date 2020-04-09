package uk.gov.hmcts.reform.iahomeofficeintegrationapi;

import static org.assertj.core.api.Assertions.assertThat;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.model.RequestResponsePact;
import com.google.common.collect.Maps;
import groovy.util.logging.Slf4j;
import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.parsing.Parser;
import java.util.Map;
import net.serenitybdd.rest.SerenityRest;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Slf4j
@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
public class HomeOfficeConsumerTest {

    private static final String HOMEOFFICE_API_URL = "/searchByParameters";

    @BeforeEach
    public void setUp() {
        RestAssured.defaultParser = Parser.JSON;
        RestAssured.config().encoderConfig(new EncoderConfig("UTF-8", "UTF-8"));
    }

    @Pact(provider = "homeoffice_api", consumer = "hmcts")
    public RequestResponsePact executeSearchByParametersAndGet200(PactDslWithProvider builder) {

        Map<String, String> headers = Maps.newHashMap();
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        return builder
            .given("Homeoffice successfully returns appeal details")
            .uponReceiving("Provider receives a POST /searchByParameters request from an IA API")
            .path(HOMEOFFICE_API_URL)
            .headers(headers)
            .method(HttpMethod.POST.toString())
            .body(new PactDslJsonBody()
                .stringType("ho_reference", "1234-5678-6789-7890"))
            .willRespondWith()
            .status(HttpStatus.OK.value())
            .body(new PactDslJsonBody()
                .stringType("ho_reference", "1234-5678-6789-7890")
                .stringType("appealDecisionDate", "20-02-2020")
                .stringType("firstName", "20-02-2020")
                .stringType("surName", "20-02-2020"))
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "executeSearchByParametersAndGet200")
    public void should_get_appeal_details_with_ho_reference(MockServer mockServer) throws JSONException {

        String actualResponseBody =
            SerenityRest
                .given()
                .relaxedHTTPSValidation()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .post(mockServer.getUrl() + HOMEOFFICE_API_URL)
                .then()
                .statusCode(200)
                .and()
                .extract()
                .body()
                .asString();

        JSONObject response = new JSONObject(actualResponseBody);

        assertThat(actualResponseBody).isNotNull();
        assertThat(response).hasNoNullFieldsOrProperties();
        assertThat(response.getString("ho_reference")).isNotBlank();
        assertThat(response.getString("firstName")).isNotBlank();
        assertThat(response.getString("surname")).isNotBlank();
        assertThat(response.getString("appealDecisionDate")).isNotBlank();

    }
}
