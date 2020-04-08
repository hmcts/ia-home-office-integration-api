package uk.gov.hmcts.reform.iahomeofficeintegrationapi;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

public class SmokeTest {

    private static final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:8098"
        );

    @Test
    public void should_prove_app_is_running_and_healthy() throws Exception {

        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();

        String response = given()
            .when()
            .get("/health")
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract().body().asString();

        assertThat(response)
            .contains("UP");
    }
}
