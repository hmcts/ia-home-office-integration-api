package uk.gov.hmcts.reform.iahomeofficeintegrationapi.component.testutils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.serviceUnavailable;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

public interface WithHomeOfficeAuthStub {

    default void addHomeOfficeAuthTokenStub(WireMockServer server) {

        String jwtToken = "{"
                          + "\"access_token\": \"some_access_token\","
                          + "\"expires_in\": 300,"
                          + "\"token_type\": \"bearer\","
                          + "\"not-before-policy\": 0,"
                          + "\"scope\": \"email profile\""
                          + "}";

        server.addStubMapping(
            new StubMapping(
                newRequestPattern(RequestMethod.POST, urlEqualTo("/ichallenge/token"))
                    .withHeader("Content-Type", containing("application/x-www-form-urlencoded"))
                    .withRequestBody(
                        equalTo("grant_type=client_credentials"
                                + "&client_secret=something"
                                + "&client_id=ho-client-id"
                        )
                    )
                    .build(),
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json;charset=UTF-8")
                    // a JWT token as response body
                    .withBody(jwtToken)
                    .build()
            )
        );
    }

    default void addHomeOfficeAuthToken503ServiceUnavailableStub(WireMockServer server) {

        server.addStubMapping(
            new StubMapping(
                newRequestPattern(RequestMethod.POST, urlEqualTo("/ichallenge/token"))
                    .withHeader("Content-Type", containing("application/x-www-form-urlencoded"))
                    .withRequestBody(
                        equalTo("grant_type=client_credentials"
                                + "&client_secret=something"
                                + "&client_id=ho-client-id"
                        )
                    )
                    .build(),
                serviceUnavailable().build()
            )
        );
    }
}
