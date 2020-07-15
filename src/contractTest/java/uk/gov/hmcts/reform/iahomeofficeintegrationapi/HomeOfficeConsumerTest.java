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
import org.json.JSONArray;
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

    private static final String HOMEOFFICE_API_SEARCH_URL = "/applicationStatus/getBySearchParameters";
    private static final String HOMEOFFICE_API_INSTRUCT_URL = "/applicationInstruct/setInstruct";

    @BeforeEach
    public void setUp() {
        RestAssured.defaultParser = Parser.JSON;
        RestAssured.config().encoderConfig(new EncoderConfig("UTF-8", "UTF-8"));
    }

    @Pact(provider = "homeoffice_api", consumer = "hmcts")
    public RequestResponsePact executeSearchByParametersAndGet200(PactDslWithProvider builder) {

        Map<String, String> headers = Maps.newHashMap();
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        //DO NOT CHANGE THE INDENTATION OF THE BODY
        PactDslJsonBody validateResponseBody = new PactDslJsonBody();
        validateResponseBody
            .stringMatcher("messageType","RESPONSE_RIGHT_OF_APPEAL_DETAILS","RESPONSE_RIGHT_OF_APPEAL_DETAILS")
            .object("messageHeader")
                .stringType("correlationId", "ABC2344BCED2234EA")
                .stringType("eventDateTime", "2017-07-21T17:32:28Z")
                .object("consumer")
                    .stringMatcher("code", "HMCTS", "HMCTS")
                    .stringType("description", "HM Courts and Tribunal Service")
                .closeObject()
            .closeObject()
            .minArrayLike("status", 1)
                .object("applicationStatus")
                .stringType("decisionDate", "2017-07-21T17:32:28Z")
                .stringType("documentReference", "1234-1234-5678-5678/00")
                    .object("applicationType")
                        .stringType("description", "Asylum and Protection")
                    .closeObject()
                    .object("claimReasonType")
                        .stringType("description", "Human Rights")
                    .closeObject()
                    .object("decisionCommunication")
                        .stringType("description","E-mail")
                        .stringType("dispatchDate", "2017-07-21T17:32:28Z")
                        .stringType("sentDate", "2017-07-21T17:32:28Z")
                        .stringMatcher("type", "EMAIL|POST", "EMAIL")
                    .closeObject()
                    .object("decisionType")
                        .stringType("description", "Rejected")
                    .closeObject()
                    .minArrayLike("metadata", 0, 1)
                        .stringMatcher("code", "APPEALABLE|DISPATCH_DATE|SUSPENSIVE|IN_COUNTRY","APPEALABLE")
                        .booleanType("valueBoolean", true)
                        .stringType("valueDateTime", "2017-07-21T17:32:28Z")
                        .stringType("valueString", "Some extra decision data")
                    .closeObject().closeArray()
                    .minArrayLike("rejectionReasons", 0, 1)
                        .stringType("reason", "Application not completed properly")
                    .closeObject().closeArray()
                    .object("roleSubType")
                        .stringType("description", "Spouse")
                    .closeObject()
                    .object("roleType")
                        .stringType("description", "Dependant")
                    .closeObject()
                .closeObject() //applicationStatus closed
                .object("person")
                    .integerType("dayOfBirth", 21)
                    .stringType("familyName", "Smith")
                    .stringType("fullName", "Capability Smith")
                    .stringType("givenName", "Capability")
                    .integerType("monthOfBirth", 3)
                    .integerType("yearOfBirth", 1970)
                        .object("gender")
                            .stringType("description", "Male")
                        .closeObject()
                        .object("nationality")
                            .stringType("description", "Canada")
                        .closeObject()
                .closeObject() //person closed
            .closeObject().closeArray() //status array closed
            .closeObject();

        return builder
            .given("Home Office successfully returns appeal details")
            .uponReceiving("Provider receives a POST /applicationStatus/getBySearchParameters request from an IA API")
            .path(HOMEOFFICE_API_SEARCH_URL)
            .headers(headers)
            .method(HttpMethod.POST.toString())
            .willRespondWith()
            .status(HttpStatus.OK.value())
            .body(validateResponseBody)
            .toPact();
    }

    @Pact(provider = "homeoffice_api", consumer = "hmcts")
    public RequestResponsePact executeSetInstructAndGet200(PactDslWithProvider builder) {

        Map<String, String> headers = Maps.newHashMap();
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        //DO NOT CHANGE THE INDENTATION OF THE BODY
        PactDslJsonBody instructResponseBody = new PactDslJsonBody();
        instructResponseBody
            .object("messageHeader")
                .stringType("correlationId", "ABC2344BCED2234EA")
                .stringType("eventDateTime", "2017-07-21T17:32:28Z")
                .object("consumer")
                    .stringMatcher("code", "HMCTS", "HMCTS")
                    .stringType("description", "HM Courts and Tribunal Service")
                .closeObject()
            .closeObject()
            .closeObject();

        return builder
            .given("Home Office successfully receives HMCTS notification")
            .uponReceiving("Provider receives a POST /applicationInstruct/setInstruct request from an IA API")
            .path(HOMEOFFICE_API_INSTRUCT_URL)
            .headers(headers)
            .method(HttpMethod.POST.toString())
            .willRespondWith()
            .status(HttpStatus.OK.value())
            .body(instructResponseBody)
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
                .post(mockServer.getUrl() + HOMEOFFICE_API_SEARCH_URL)
                .then()
                .statusCode(200)
                .and()
                .extract()
                .body()
                .asString();

        JSONObject response = new JSONObject(actualResponseBody);
        JSONArray status = response.getJSONArray("status");

        assertThat(actualResponseBody).isNotNull();
        assertThat(response).hasNoNullFieldsOrProperties();
        assertThat(response.getJSONObject("messageHeader")).isNotNull();
        assertThat(response.getJSONObject("messageHeader").getString("correlationId")).isNotBlank();
        assertThat(response.getJSONObject("messageHeader").getString("eventDateTime")).isNotBlank();
        assertThat(response.getJSONObject("messageHeader").getJSONObject("consumer")).isNotNull();
        assertThat(response.getJSONObject("messageHeader").getJSONObject("consumer").getString("code"))
            .matches("HMCTS");
        assertThat(response.getJSONObject("messageHeader").getJSONObject("consumer").getString("description"))
            .isNotBlank();
        assertThat(response.getString("messageType")).matches("RESPONSE_RIGHT_OF_APPEAL_DETAILS");
        assertThat(status).isNotNull();
        assertThat(status.length()).isGreaterThan(0);

        JSONObject applicationStatus = status.getJSONObject(0).getJSONObject("applicationStatus");
        JSONObject person = status.getJSONObject(0).getJSONObject("person");

        assertThat(applicationStatus).isNotNull();
        assertThat(applicationStatus.getString("decisionDate")).isNotBlank();
        assertThat(applicationStatus.getString("documentReference")).isNotBlank();
        assertThat(applicationStatus.getJSONObject("applicationType")).isNotNull();
        assertThat(applicationStatus.getJSONObject("applicationType").getString("description")).isNotBlank();
        assertThat(applicationStatus.getJSONObject("claimReasonType")).isNotNull();
        assertThat(applicationStatus.getJSONObject("claimReasonType").getString("description")).isNotBlank();
        assertThat(applicationStatus.getJSONObject("decisionCommunication")).isNotNull();
        assertThat(applicationStatus.getJSONObject("decisionCommunication").getString("description")).isNotBlank();
        assertThat(applicationStatus.getJSONObject("decisionCommunication").getString("dispatchDate")).isNotBlank();
        assertThat(applicationStatus.getJSONObject("decisionCommunication").getString("sentDate")).isNotBlank();
        assertThat(applicationStatus.getJSONObject("decisionCommunication").getString("type")).matches("EMAIL|POST");
        assertThat(applicationStatus.getJSONObject("decisionType")).isNotNull();
        assertThat(applicationStatus.getJSONObject("decisionType").getString("description")).isNotBlank();
        assertThat(applicationStatus.getJSONArray("metadata")).isNotNull();
        assertThat(applicationStatus.getJSONArray("metadata").length()).isGreaterThanOrEqualTo(0);
        JSONObject metadata = applicationStatus.getJSONArray("metadata").getJSONObject(0);
        assertThat(metadata.getString("code")).matches("APPEALABLE|DISPATCH_DATE|SUSPENSIVE|IN_COUNTRY");
        assertThat(metadata.getBoolean("valueBoolean")).isNotNull();
        assertThat(metadata.getString("valueDateTime")).isNotBlank();
        assertThat(metadata.getString("valueString")).isNotBlank();
        assertThat(applicationStatus.getJSONArray("rejectionReasons")).isNotNull();
        assertThat(applicationStatus.getJSONArray("rejectionReasons").length()).isGreaterThanOrEqualTo(0);
        assertThat(applicationStatus.getJSONArray("rejectionReasons")
            .getJSONObject(0).getString("reason")).isNotBlank();
        assertThat(applicationStatus.getJSONObject("roleSubType")).isNotNull();
        assertThat(applicationStatus.getJSONObject("roleSubType").getString("description")).isNotBlank();
        assertThat(applicationStatus.getJSONObject("roleType")).isNotNull();
        assertThat(applicationStatus.getJSONObject("roleType").getString("description")).isNotBlank();

        assertThat(person).isNotNull();
        assertThat(person.getString("givenName")).isNotBlank();
        assertThat(person.getString("familyName")).isNotBlank();
        assertThat(person.getString("fullName")).isNotBlank();
        assertThat(person.getInt("dayOfBirth")).isGreaterThan(0);
        assertThat(person.getInt("monthOfBirth")).isGreaterThan(0);
        assertThat(person.getInt("yearOfBirth")).isGreaterThan(0);
        assertThat(person.getJSONObject("gender")).isNotNull();
        assertThat(person.getJSONObject("gender").getString("description")).isNotBlank();
        assertThat(person.getJSONObject("nationality")).isNotNull();
        assertThat(person.getJSONObject("nationality").getString("description")).isNotBlank();

    }

    @Test
    @PactTestFor(pactMethod = "executeSetInstructAndGet200")
    public void should_get_success_for_sending_notification(MockServer mockServer) throws JSONException {

        String actualResponseBody =
            SerenityRest
                .given()
                .relaxedHTTPSValidation()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .post(mockServer.getUrl() + HOMEOFFICE_API_INSTRUCT_URL)
                .then()
                .statusCode(200)
                .and()
                .extract()
                .body()
                .asString();

        JSONObject response = new JSONObject(actualResponseBody);

        assertThat(actualResponseBody).isNotNull();
        assertThat(response).hasNoNullFieldsOrProperties();
        assertThat(response.getJSONObject("messageHeader")).isNotNull();
        assertThat(response.getJSONObject("messageHeader").getString("correlationId")).isNotBlank();
        assertThat(response.getJSONObject("messageHeader").getString("eventDateTime")).isNotBlank();
        assertThat(response.getJSONObject("messageHeader").getJSONObject("consumer")).isNotNull();
        assertThat(response.getJSONObject("messageHeader").getJSONObject("consumer").getString("code"))
            .matches("HMCTS");
        assertThat(response.getJSONObject("messageHeader").getJSONObject("consumer").getString("description"))
            .isNotBlank();

    }
}
