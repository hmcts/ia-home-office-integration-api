package uk.gov.hmcts.reform.iahomeofficeintegrationapi;

import static org.junit.Assert.assertFalse;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.RetryableException;
import io.restassured.RestAssured;
import io.restassured.http.Headers;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.serenitybdd.rest.SerenityRest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.util.AuthorizationHeadersProvider;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.util.LaunchDarklyFunctionalTestClient;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.util.MapMerger;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.util.MapSerializer;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.util.MapValueExpander;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.util.MapValueExtractor;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.util.StringResourceLoader;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.verifiers.Verifier;


@RunWith(SpringIntegrationSerenityRunner.class)
@SpringBootTest
@ActiveProfiles("functional")
public class CcdScenarioRunnerTest {

    @Value("${targetInstance}")
    private String targetInstance;

    @Autowired
    private Environment environment;
    @Autowired
    private AuthorizationHeadersProvider authorizationHeadersProvider;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private List<Verifier> verifiers;
    private boolean haveAllPassed = true;
    private final ArrayList<String> failedScenarios = new ArrayList<>();
    @Autowired
    private LaunchDarklyFunctionalTestClient launchDarklyFunctionalTestClient;

    @BeforeEach
    public void setUp() {
        MapSerializer.setObjectMapper(objectMapper);
        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Test
    public void scenarios_should_behave_as_specified() throws IOException {

        assertFalse(
                "Verifiers are configured",
                verifiers.isEmpty()
        );

        loadPropertiesIntoMapValueExpander();

        String scenarioPattern = System.getProperty("scenario");
        System.out.println("scenarioPattern:" + scenarioPattern);
        if (scenarioPattern == null) {
            scenarioPattern = "*.json";
        } else {
            scenarioPattern = "*" + scenarioPattern + "*.json";
        }

        Collection<String> scenarioSources =
                StringResourceLoader
                        .load("/scenarios/" + scenarioPattern)
                        .values();

        System.out.println((char) 27 + "[36m" + "-------------------------------------------------------------------");
        System.out.println((char) 27 + "[33m" + "RUNNING " + scenarioSources.size() + " SCENARIOS");
        System.out.println((char) 27 + "[36m" + "-------------------------------------------------------------------");

        int maxRetries = 3;
        for (String scenarioSource : scenarioSources) {
            String description = "";
            for (int i = 0; i < maxRetries; i++) {
                try {
                    Map<String, Object> scenario = deserializeWithExpandedValues(scenarioSource);

                    final Headers authorizationHeaders = getAuthorizationHeaders(scenario);

                    description = MapValueExtractor.extract(scenario, "description");

                    Object scenarioEnabled = MapValueExtractor.extract(scenario, "enabled") == null
                            ? MapValueExtractor.extract(scenario, "launchDarklyKey")
                            : MapValueExtractor.extract(scenario, "enabled");

                    if (scenarioEnabled == null) {
                        scenarioEnabled = true;
                    } else if (scenarioEnabled instanceof String) {

                        if (String.valueOf(scenarioEnabled).contains("feature")) {

                            String[] keys = ((String) scenarioEnabled).split(":");

                            scenarioEnabled = launchDarklyFunctionalTestClient
                                    .getKey(keys[0], authorizationHeaders.getValue("Authorization"))
                                    && Boolean.valueOf(keys[1]);
                        } else {
                            scenarioEnabled = Boolean.valueOf((String) scenarioEnabled);
                        }
                    }

                    Object scenarioDisabled = MapValueExtractor.extract(scenario, "disabled");

                    if (scenarioDisabled == null) {
                        scenarioDisabled = false;
                    } else if (scenarioDisabled instanceof String) {
                        scenarioDisabled = Boolean.valueOf((String) scenarioDisabled);
                    }

                    if (!((Boolean) scenarioEnabled) || ((Boolean) scenarioDisabled)) {
                        System.out.println((char) 27 + "[31m" + "SCENARIO: " + description + " **disabled**");
                        continue;
                    }

                    System.out.println((char) 27 + "[33m" + "SCENARIO: " + description);

                    Map<String, String> templatesByFilename = StringResourceLoader.load("/templates/*.json");

                    final long testCaseId = MapValueExtractor.extractOrDefault(
                            scenario,
                            "request.input.id",
                            ThreadLocalRandom.current().nextInt(1, 9999999 + 1)

                    );

                    final String requestBody = buildCallbackBody(
                            testCaseId,
                            MapValueExtractor.extract(scenario, "request.input"),
                            templatesByFilename
                    );

                    final String requestUri = MapValueExtractor.extract(scenario, "request.uri");
                    final int expectedStatus = MapValueExtractor.extractOrDefault(scenario, "expectation.status", 200);

                    String actualResponseBody =
                            SerenityRest
                                    .given()
                                    .headers(authorizationHeaders)
                                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                                    .body(requestBody)
                                    .when()
                                    .post(requestUri)
                                    .then()
                                    .statusCode(expectedStatus)
                                    .and()
                                    .extract()
                                    .body()
                                    .asString();

                    System.out.println("Response body: " + actualResponseBody);

                    String expectedResponseBody = buildCallbackResponseBody(
                            MapValueExtractor.extract(scenario, "expectation"),
                            templatesByFilename
                    );

                    Map<String, Object> actualResponse = MapSerializer.deserialize(actualResponseBody);
                    Map<String, Object> expectedResponse = MapSerializer.deserialize(expectedResponseBody);

                    verifiers.forEach(verifier -> verifier.verify(
                                    testCaseId,
                                    scenario,
                                    expectedResponse,
                                    actualResponse
                            )
                    );
                    break;
                } catch (Error | RetryableException e) {
                    System.out.println("Scenario failed with error " + e.getMessage());
                    if (i == maxRetries - 1) {
                        this.failedScenarios.add(description);
                        this.haveAllPassed = false;
                    }
                }
            }
        }
        System.out.println((char) 27 + "[36m" + "-------------------------------------------------------------------");
        System.out.println((char) 27 + "[0m");
        if (!haveAllPassed) {
            throw new AssertionError("Not all scenarios passed.\nFailed scenarios are:\n" + failedScenarios.stream().map(Object::toString).collect(Collectors.joining(";\n")));
        }
    }

    private void loadPropertiesIntoMapValueExpander() {

        MutablePropertySources propertySources = ((AbstractEnvironment) environment).getPropertySources();
        StreamSupport
            .stream(propertySources.spliterator(), false)
            .filter(propertySource -> propertySource instanceof EnumerablePropertySource)
            .map(propertySource -> ((EnumerablePropertySource) propertySource).getPropertyNames())
            .flatMap(Arrays::stream)
            .forEach(name -> MapValueExpander.ENVIRONMENT_PROPERTIES.setProperty(name, environment.getProperty(name)));
    }

    private Map<String, Object> deserializeWithExpandedValues(
        String source
    ) throws IOException {
        Map<String, Object> data = MapSerializer.deserialize(source);
        MapValueExpander.expandValues(data);
        return data;
    }

    private Map<String, Object> buildCaseData(
        Map<String, Object> caseDataInput,
        Map<String, String> templatesByFilename
    ) throws IOException {

        String templateFilename = MapValueExtractor.extract(caseDataInput, "template");

        Map<String, Object> caseData = deserializeWithExpandedValues(templatesByFilename.get(templateFilename));
        Map<String, Object> caseDataReplacements = MapValueExtractor.extract(caseDataInput, "replacements");
        if (caseDataReplacements != null) {
            MapMerger.merge(caseData, caseDataReplacements);
        }

        return caseData;
    }

    private String buildCallbackBody(
        long testCaseId,
        Map<String, Object> input,
        Map<String, String> templatesByFilename
    ) throws IOException {

        Map<String, Object> caseData = buildCaseData(
            MapValueExtractor.extract(input, "caseData"),
            templatesByFilename
        );

        LocalDateTime createdDate =
            LocalDateTime.parse(
                MapValueExtractor.extractOrDefault(input, "createdDate", LocalDateTime.now().toString())
            );

        Map<String, Object> caseDetails = new HashMap<>();
        caseDetails.put("id", testCaseId);
        caseDetails.put("jurisdiction", MapValueExtractor.extractOrDefault(input, "jurisdiction", "IA"));
        caseDetails.put("state", MapValueExtractor.extractOrThrow(input, "state"));
        caseDetails.put("created_date", createdDate);
        caseDetails.put("case_data", caseData);

        Map<String, Object> callback = new HashMap<>();
        callback.put("event_id", MapValueExtractor.extractOrThrow(input, "eventId"));
        callback.put("case_details", caseDetails);

        if (input.containsKey("caseDataBefore")) {
            Map<String, Object> caseDataBefore = buildCaseData(
                MapValueExtractor.extract(input, "caseDataBefore"),
                templatesByFilename
            );

            Map<String, Object> caseDetailsBefore = new HashMap<>();
            caseDetailsBefore.put("id", testCaseId);
            caseDetailsBefore.put("jurisdiction", MapValueExtractor.extractOrDefault(input, "jurisdiction", "IA"));
            caseDetailsBefore.put("state", MapValueExtractor.extractOrThrow(input, "state"));
            caseDetailsBefore.put("created_date", createdDate);
            caseDetailsBefore.put("case_data", caseDataBefore);
            callback.put("case_details_before", caseDetailsBefore);
        }

        return MapSerializer.serialize(callback);
    }

    private String buildCallbackResponseBody(
        Map<String, Object> expectation,
        Map<String, String> templatesByFilename
    ) throws IOException {

        if (MapValueExtractor.extract(expectation, "confirmation") != null) {

            final Map<String, Object> callbackResponse = new HashMap<>();

            callbackResponse.put("confirmation_header", MapValueExtractor.extract(expectation, "confirmation.header"));
            callbackResponse.put("confirmation_body", MapValueExtractor.extract(expectation, "confirmation.body"));

            return MapSerializer.serialize(callbackResponse);

        } else {

            Map<String, Object> caseData = buildCaseData(
                MapValueExtractor.extract(expectation, "caseData"),
                templatesByFilename
            );

            PreSubmitCallbackResponse<AsylumCase> preSubmitCallbackResponse =
                new PreSubmitCallbackResponse<>(
                    objectMapper.readValue(
                        MapSerializer.serialize(caseData),
                        new TypeReference<AsylumCase>() {
                        }
                    )
                );

            preSubmitCallbackResponse.addErrors(MapValueExtractor.extract(expectation, "errors"));

            return objectMapper.writeValueAsString(preSubmitCallbackResponse);
        }
    }

    private Headers getAuthorizationHeaders(Map<String, Object> scenario) {

        String credentials = MapValueExtractor.extract(scenario, "request.credentials");

        if ("LegalRepresentative".equalsIgnoreCase(credentials)) {

            return authorizationHeadersProvider
                .getLegalRepresentativeAuthorization();
        }

        if ("CaseOfficer".equalsIgnoreCase(credentials)) {

            return authorizationHeadersProvider
                .getCaseOfficerAuthorization();
        }

        if ("AdminOfficer".equalsIgnoreCase(credentials)) {

            return authorizationHeadersProvider
                .getAdminOfficerAuthorization();
        }

        if ("Judge".equalsIgnoreCase(credentials)) {

            return authorizationHeadersProvider
                .getJudgeAuthorization();
        }

        if ("HomeOfficePou".equalsIgnoreCase(credentials)) {

            return authorizationHeadersProvider
                .getHomeOfficePouAuthorization();
        }

        if ("HomeOfficeGeneric".equalsIgnoreCase(credentials)) {

            return authorizationHeadersProvider
                .getHomeOfficeGenericAuthorization();
        }

        return new Headers();
    }
}
