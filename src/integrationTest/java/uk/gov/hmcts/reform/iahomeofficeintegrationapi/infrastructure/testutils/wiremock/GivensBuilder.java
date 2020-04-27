package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.testutils.wiremock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.testutils.fixtures.Builder;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.testutils.fixtures.UserDetailsForTest;

@SuppressWarnings("OperatorWrap")
public class GivensBuilder {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public GivensBuilder someLoggedIn(UserDetailsForTest.UserDetailsForTestBuilder userDetailsForTestBuilder) {

        stubFor(get(urlEqualTo("/userAuth/details"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(
                    getObjectAsJsonString(
                        userDetailsForTestBuilder))));

        return this;
    }

    private String getObjectAsJsonString(Builder builder) {

        try {
            return objectMapper.writeValueAsString(builder.build());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Couldn't serialize object", e);
        }
    }

    public GivensBuilder and() {
        return this;
    }
}
