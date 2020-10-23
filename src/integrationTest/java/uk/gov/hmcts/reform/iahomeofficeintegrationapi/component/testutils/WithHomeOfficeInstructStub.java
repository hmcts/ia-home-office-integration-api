package uk.gov.hmcts.reform.iahomeofficeintegrationapi.component.testutils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.serviceUnavailable;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.component.testutils.IntegrationConstants.HOME_OFFICE_ACCESS_TOKEN;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.springframework.util.ResourceUtils;


public interface WithHomeOfficeInstructStub {

    String HOME_OFFICE_INSTRUCT_API_URL = "/ichallenge/applicationInstruct/setInstruct";

    default void addHomeOfficeApiInstructStub(WireMockServer server, String homeOfficeReference) throws IOException {

        final File hoInstructResponseResourceFile
            = ResourceUtils.getFile("classpath:ho-api_instruct_200-valid_response.json");
        String hoInstructResponseJson
            = new String(Files.readAllBytes(hoInstructResponseResourceFile.toPath()));

        server.addStubMapping(
            new StubMapping(
                newRequestPattern(RequestMethod.POST, urlEqualTo(HOME_OFFICE_INSTRUCT_API_URL))
                    .withHeader("Authorization", equalTo("Bearer " + HOME_OFFICE_ACCESS_TOKEN))
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

    default void addHomeOfficeApiInstruct500ServerErrorWithResponseStub(
        WireMockServer server, String homeOfficeReference) {

        server.addStubMapping(
            new StubMapping(
                newRequestPattern(RequestMethod.POST, urlEqualTo(HOME_OFFICE_INSTRUCT_API_URL))
                    .withHeader("Authorization", equalTo("Bearer " + HOME_OFFICE_ACCESS_TOKEN))
                    .withRequestBody(
                        matchingJsonPath("$.hoReference", equalTo(homeOfficeReference)))
                    .build(),
                aResponse()
                    .withStatus(500)
                    .withBody("Internal Server Error")
                    .build()
            )
        );
    }


    default void addHomeOfficeApiInstruct503ServiceUnavailableErrorStub(
        WireMockServer server, String homeOfficeReference) {

        server.addStubMapping(
            new StubMapping(
                newRequestPattern(RequestMethod.POST, urlEqualTo(HOME_OFFICE_INSTRUCT_API_URL))
                    .withHeader("Authorization", equalTo("Bearer " + HOME_OFFICE_ACCESS_TOKEN))
                    .withRequestBody(
                        matchingJsonPath("$.hoReference", equalTo(homeOfficeReference)))
                    .build(),
                serviceUnavailable().build()
            )
        );
    }

    default void addHomeOfficeApiInstruct500InternalServerErrorStub(WireMockServer server, String homeOfficeReference) {

        server.addStubMapping(
            new StubMapping(
                newRequestPattern(RequestMethod.POST, urlEqualTo(HOME_OFFICE_INSTRUCT_API_URL))
                    .withHeader("Authorization", equalTo("Bearer " + HOME_OFFICE_ACCESS_TOKEN))
                    .withRequestBody(
                        matchingJsonPath("$.hoReference", equalTo(homeOfficeReference)))
                    .build(),
                serverError().build()
            )
        );
    }


}
