package uk.gov.hmcts.reform.iahomeofficeintegrationapi.component.testutils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.component.testutils.IntegrationConstants.HOME_OFFICE_ACCESS_TOKEN;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.component.testutils.IntegrationConstants.HOME_OFFICE_API_URL;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.ResourceUtils;

public interface WithHomeOfficeStatusSearchStub {



    default void addHomeOfficeApiSearchValidResponseStub(WireMockServer server, String homeOfficeReference)
        throws IOException {

        String hoGetBySearchParamsResponseJson = getResponseJson(homeOfficeReference);
        addStubMappingWithStatus(server, hoGetBySearchParamsResponseJson, homeOfficeReference, 200);
    }

    default void addHomeOfficeApiSearchNoInvolvementsStub(WireMockServer server, String homeOfficeReference)
        throws IOException {

        String hoGetBySearchParamsResponseJson = getResponseJson(homeOfficeReference);
        addStubMappingWithStatus(server, hoGetBySearchParamsResponseJson, homeOfficeReference, 200);
    }

    default void addHomeOfficeApiSearchExtraFieldsStub(WireMockServer server, String homeOfficeReference)
        throws IOException {

        String hoGetBySearchParamsResponseJson = getResponseJson(homeOfficeReference);
        addStubMappingWithStatus(server, hoGetBySearchParamsResponseJson, homeOfficeReference, 200);
    }

    default void addHomeOfficeApiSearch400InternalSystemErrorStub(WireMockServer server, String homeOfficeReference)
        throws IOException {

        String hoGetBySearchParamsResponseJson = getResponseJson(homeOfficeReference);
        addStubMappingWithStatus(server, hoGetBySearchParamsResponseJson, homeOfficeReference, 400);
    }

    default void addHomeOfficeApiSearch400BadRequestStub(WireMockServer server, String homeOfficeReference)
        throws IOException {

        String hoGetBySearchParamsResponseJson = getResponseJson(homeOfficeReference);
        addStubMappingWithStatus(server, hoGetBySearchParamsResponseJson, homeOfficeReference, 400);
    }

    default void addHomeOfficeApiSearch500ServerErrorStub(WireMockServer server, String homeOfficeReference) {

        server.addStubMapping(
            new StubMapping(
                newRequestPattern(RequestMethod.POST, urlEqualTo(HOME_OFFICE_API_URL))
                    .withHeader("Authorization", equalTo(HOME_OFFICE_ACCESS_TOKEN))
                    .withRequestBody(
                        matchingJsonPath("$.searchParams[0].spValue", equalTo(homeOfficeReference)))
                    .build(),
                aResponse()
                    .withStatus(500)
                    .withBody("Internal Server Error")
                    .build()
            )
        );
    }

    @NotNull
    private String getResponseJson(String homeOfficeReference) throws IOException {

        String responseJsonFile;

        switch (homeOfficeReference) {
            case "CustRef123":
                responseJsonFile = "ho-api_get-by-search-params_200-valid_response.json";
                break;
            case "extra-fields-ref-number":
                responseJsonFile = "ho-api_get-by-search-params_200-extra-fields_response.json";
                break;
            case "1212-0099-0036-2016":
                responseJsonFile = "ho-api_get-by-search-params_200-no-involvements_response.json";
                break;
            case "1212-0099-0036-1000":
                responseJsonFile = "ho-api_get-by-search-params_400-internal-server-error_response.json";
                break;
            case "1212-0099-0036-XXXX":
                responseJsonFile = "ho-api_get-by-search-params_400-bad-request_response.json";
                break;
            case "CustRef000":
                responseJsonFile = "ho-api_get-by-search-params_200-valid_null_response.json";
                break;
            default:
                throw new IOException("Invalid home office reference : " + homeOfficeReference);
        }

        final File hoSearchResponseResourceFile
            = ResourceUtils.getFile("classpath:" + responseJsonFile);

        return new String(Files.readAllBytes(hoSearchResponseResourceFile.toPath()));
    }

    private void addStubMappingWithStatus(
        final WireMockServer server,
        final String hoGetBySearchParamsResponseJson,
        final String homeOfficeReference,
        int status) {

        server.addStubMapping(
            new StubMapping(
                newRequestPattern(RequestMethod.POST, urlEqualTo(HOME_OFFICE_API_URL))
                    .withHeader("Authorization", equalTo("Bearer " + HOME_OFFICE_ACCESS_TOKEN))
                    .withRequestBody(
                        matchingJsonPath("$.searchParams[0].spValue", equalTo(homeOfficeReference)))
                    .build(),
                aResponse()
                    .withStatus(status)
                    .withHeader("Content-Type", "application/json")
                    .withBody(hoGetBySearchParamsResponseJson)
                    .build()
            )
        );
    }
}
