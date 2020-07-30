package uk.gov.hmcts.reform.iahomeofficeintegrationapi.component.testutils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.component.testutils.IntegrationConstants.USER_ID;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.component.testutils.IntegrationConstants.USER_TOKEN;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

public interface WithIdamStub {

    default void addIdamTokenStub(WireMockServer server) {
        server.addStubMapping(
            new StubMapping(
                newRequestPattern(RequestMethod.POST, urlEqualTo("/idam/o/token"))
                    .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded;charset=UTF-8"))
                    .withRequestBody(
                        equalTo("grant_type=password"
                                + "&redirect_uri=http%3A%2F%2Flocalhost%3A3002%2Foauth2%2Fcallback"
                                + "&client_id=ia"
                                + "&client_secret=something"
                                + "&username=ia-system-user%40fake.hmcts.net"
                                + "&password=London05"
                                + "&scope=openid+profile+roles"
                        )
                    )
                    .build(),
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"access_token\": \"" + USER_TOKEN + "\"}")
                    .build()
            )
        );
    }

    default void addUserInfoStub(WireMockServer server) {
        server.addStubMapping(
            new StubMapping(
                newRequestPattern(RequestMethod.GET, urlEqualTo("/idam/o/userinfo"))
                    .withHeader("Authorization", equalTo("Bearer " + USER_TOKEN))
                    .build(),
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"uid\": \"" + USER_ID + "\"}")
                    .build()
            )
        );
    }

    default void addUserDetailsStub(WireMockServer server) {
        server.addStubMapping(
            new StubMapping(
                newRequestPattern(RequestMethod.GET, urlEqualTo("/idam/details"))
                    .withHeader("Authorization", equalTo(USER_TOKEN))
                    .build(),
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{"
                        + "\"id\": \"" + USER_ID + "\","
                        + "\"roles\": [\"caseworker-ia-legalrep-solicitor\"],"
                        + "\"email\": \"user@fakeaddress.com\","
                        + "\"forename\": \"forename\","
                        + "\"surname\": \"surname\""
                        + "}")
                    .build()
            )
        );
    }
}
