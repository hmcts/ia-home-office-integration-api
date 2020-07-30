package uk.gov.hmcts.reform.iahomeofficeintegrationapi.component.testutils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.component.testutils.IntegrationConstants.SERVICE_TOKEN;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

public interface WithServiceAuthStub {

    default void addServiceAuthStub(WireMockServer server) {

        server.addStubMapping(
            new StubMapping(
                newRequestPattern(RequestMethod.GET, urlEqualTo("/s2s/details"))
                    .build(),
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("ia")
                    .build()));

        server.addStubMapping(
            new StubMapping(
                newRequestPattern(RequestMethod.POST, urlEqualTo("/s2s/lease"))
                    .build(),
                aResponse()
                    .withStatus(200)
                    .withBody(SERVICE_TOKEN)
                    .build()));
    }
}
