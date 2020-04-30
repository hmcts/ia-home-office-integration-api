package uk.gov.hmcts.reform.iahomeofficeintegrationapi;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import java.util.Arrays;
import java.util.List;
import net.serenitybdd.rest.SerenityRest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.util.AuthorizationHeadersProvider;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("functional")
public class EndpointSecurityTest {

    private final List<String> callbackEndpoints =
        Arrays.asList(
            "/asylum/ccdAboutToSubmit"
        );
    @Value("${targetInstance}")
    private String targetInstance;
    @Autowired
    private AuthorizationHeadersProvider authorizationHeadersProvider;

    @BeforeEach
    public void setUp() {
        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Test
    public void should_allow_unauthenticated_requests_to_health_check_and_return_200_response_code() {

        String response =
            SerenityRest
                .when()
                .get("/health")
                .then()
                .statusCode(HttpStatus.OK.value())
                .and()
                .extract().body().asString();

        assertThat(response)
            .contains("UP");
    }

    @Test
    public void should_not_allow_unauthenticated_requests_and_return_403_response_code() {

        callbackEndpoints.forEach(callbackEndpoint ->

            SerenityRest
                .given()
                .when()
                .get(callbackEndpoint)
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value())
        );
    }

    @Test
    public void should_not_allow_requests_without_valid_service_authorisation_and_return_403_response_code() {

        String invalidServiceToken = "invalid";

        String accessToken =
            authorizationHeadersProvider
                .getLegalRepresentativeAuthorization()
                .getValue("Authorization");

        callbackEndpoints.forEach(callbackEndpoint ->

            SerenityRest
                .given()
                .header("ServiceAuthorization", invalidServiceToken)
                .header("Authorization", accessToken)
                .when()
                .get(callbackEndpoint)
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value())
        );
    }

    @Test
    public void should_not_allow_requests_without_valid_user_authorisation_and_return_403_response_code() {

        String serviceToken =
            authorizationHeadersProvider
                .getLegalRepresentativeAuthorization()
                .getValue("ServiceAuthorization");

        String invalidAccessToken = "invalid";

        callbackEndpoints.forEach(callbackEndpoint ->

            SerenityRest
                .given()
                .header("ServiceAuthorization", serviceToken)
                .header("Authorization", invalidAccessToken)
                .when()
                .post(callbackEndpoint)
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value())
        );
    }

    @Test
    public void should_allow_requests_with_valid_authorisation_tokens_and_return_200_response_code() {

        String serviceToken =
            authorizationHeadersProvider
                .getLegalRepresentativeAuthorization()
                .getValue("ServiceAuthorization");

        String accessToken = authorizationHeadersProvider
            .getLegalRepresentativeAuthorization()
            .getValue("Authorization");

        callbackEndpoints.forEach(callbackEndpoint ->

            SerenityRest
                .given()
                .header("ServiceAuthorization", serviceToken)
                .header("Authorization", accessToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .pathParam("iaHomeOfficeReference","1111-2222-3333-4444")
                .when()
                .post(callbackEndpoint)
                .then()
                .statusCode(HttpStatus.OK.value())
        );
    }
}

