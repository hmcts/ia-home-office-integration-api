package uk.gov.hmcts.reform.iahomeofficeintegrationapi.component.testutils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.springframework.util.ResourceUtils;

public interface WithHomeOfficeInstructStub {

    default void addHomeOfficeApiInstructStub(WireMockServer server, String homeOfficeReference) throws IOException {

        String homeOfficeInstructApiUrl = "/ichallenge/applicationInstruct/setInstruct";

        final File hoInstructResponseResourceFile
            = ResourceUtils.getFile("classpath:ho-api_instruct_200-valid_response.json");
        String hoInstructResponseJson
            = new String(Files.readAllBytes(hoInstructResponseResourceFile.toPath()));

        server.addStubMapping(
            new StubMapping(
                newRequestPattern(RequestMethod.POST, urlEqualTo(homeOfficeInstructApiUrl))
                    .withRequestBody(
                        matchingJsonPath("$.hoReference", equalTo(homeOfficeReference)))
                    .build(),
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(hoInstructResponseJson)
                    .build()
            )
        );
    }
}
