package uk.gov.hmcts.reform.iahomeofficeintegrationapi;

import io.restassured.RestAssured;
import net.serenitybdd.rest.SerenityRest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("functional")
public class WelcomeFunctionTest {

    @Value("${targetInstance}") private String targetInstance;

    @BeforeEach
    public void setUp() {
        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Test
    public void should_allow_unauthenticated_requests_to_welcome_message_and_return_200_response_code() {

        final String expected = "Welcome to Home Office Integration API";

        SerenityRest.given()
            .get("/")
            .then()
            .log()
            .all()
            .statusCode(HttpStatus.OK.value())
            .body("message", Matchers.equalTo(expected));
    }

}
