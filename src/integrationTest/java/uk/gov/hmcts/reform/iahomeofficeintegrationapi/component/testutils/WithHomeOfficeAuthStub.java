package uk.gov.hmcts.reform.iahomeofficeintegrationapi.component.testutils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

public interface WithHomeOfficeAuthStub {

    default void addHomeOfficeAuthTokenStub(WireMockServer server) {

        String jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhY2Nlc3NfdG9rZW4iOiJzb21lX2FjY2V"
                           + "zc190b2tlbiIsInNjb3BlIjoicmVhZCIsInRva2VuX3R5cGUiOiJiZWFyZXIiLCJleHBpc"
                           + "mVzX2luIjoyOTl9.rsrnW2pMzmSJiY_80IxvgOTgglgLGBiYtAFRRuNVVqc";

        server.addStubMapping(
            new StubMapping(
                newRequestPattern(RequestMethod.POST, urlEqualTo("/ichallenge/token"))
                    .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded;charset=UTF-8"))
                    .withRequestBody(
                        equalTo("client_id=ho-client-id"
                                + "&client_secret=something"
                                + "&grant_type=client_credentials"
                        )
                    )
                    .build(),
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "text/plain;charset=UTF-8")
                    // a JWT token as response body
                    .withBody(jwtToken)
                    .build()
            )
        );
    }

}
