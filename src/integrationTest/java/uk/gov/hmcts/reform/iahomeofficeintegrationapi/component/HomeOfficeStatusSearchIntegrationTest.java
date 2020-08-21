package uk.gov.hmcts.reform.iahomeofficeintegrationapi.component;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.component.testutils.AsylumCaseForTest.anAsylumCase;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.component.testutils.CallbackForTest.CallbackForTestBuilder.callback;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.component.testutils.CaseDetailsForTest.CaseDetailsForTestBuilder.someCaseDetailsWith;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_CASE_STATUS_DATA;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS;

import com.github.tomakehurst.wiremock.WireMockServer;
import groovy.util.logging.Slf4j;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import ru.lanwen.wiremock.ext.WiremockResolver;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.component.testutils.CallbackForTest.CallbackForTestBuilder;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.component.testutils.IaCaseHomeOfficeIntegrationApiClient;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.component.testutils.PreSubmitCallbackResponseForTest;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.component.testutils.StaticPortWiremockFactory;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.component.testutils.WithHomeOfficeAuthStub;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.component.testutils.WithHomeOfficeInstructStub;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.component.testutils.WithHomeOfficeStatusSearchStub;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.component.testutils.WithIdamStub;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.component.testutils.WithServiceAuthStub;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeCaseStatus;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.Person;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.State;

@Slf4j
public class HomeOfficeStatusSearchIntegrationTest
    extends SpringBootIntegrationTest
    implements
        WithIdamStub, WithServiceAuthStub,
        WithHomeOfficeStatusSearchStub, WithHomeOfficeInstructStub, WithHomeOfficeAuthStub {

    public static final String APPEAL_REFERENCE_NUMBER = "some-appeal-reference-number";

    @Test
    @WithMockUser(authorities = {"caseworker-ia-legalrep-solicitor"})
    public void shouldRetirieveHomeOfficeUserDetails(@WiremockResolver
        .Wiremock(factory = StaticPortWiremockFactory.class) WireMockServer server) throws Exception {

        final String homeOfficeReference = "CustRef123";

        addServiceAuthStub(server);
        addUserDetailsStub(server);
        addHomeOfficeAuthTokenStub(server);
        addHomeOfficeApiSearchValidResponseStub(server, homeOfficeReference);
        addHomeOfficeApiInstructStub(server, homeOfficeReference);

        IaCaseHomeOfficeIntegrationApiClient iaCaseHomeOfficeIntegrationApiClient
            = new IaCaseHomeOfficeIntegrationApiClient(mockMvc);

        final CallbackForTestBuilder callback = callback()
            .event(Event.SUBMIT_APPEAL)
            .caseDetails(someCaseDetailsWith()
                .state(State.APPEAL_SUBMITTED)
                .caseData(anAsylumCase()
                    .with(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, APPEAL_REFERENCE_NUMBER)
                    .with(HOME_OFFICE_REFERENCE_NUMBER, homeOfficeReference)));


        PreSubmitCallbackResponseForTest response = iaCaseHomeOfficeIntegrationApiClient.aboutToSubmit(callback);

        AsylumCase asylumCase = response.getAsylumCase();

        assertNotNull(response);
        assertNotNull(asylumCase);
        assertEquals(asylumCase.read(HOME_OFFICE_SEARCH_STATUS, String.class), Optional.of("SUCCESS"));
        final Optional<HomeOfficeCaseStatus> homeOfficeCaseStatus
            = asylumCase.read(HOME_OFFICE_CASE_STATUS_DATA, HomeOfficeCaseStatus.class);
        assertTrue(homeOfficeCaseStatus.isPresent());
        if (homeOfficeCaseStatus.isPresent()) {
            Person person = homeOfficeCaseStatus.get().getPerson();
            assertNotNull(person);
            assertEquals(person.getGivenName(), "Capability");
            assertEquals(person.getFamilyName(), "Smith");
            assertEquals(person.getNationality().getDescription(), "Canada");
            assertEquals(person.getFullName(), "Capability Smith");
        }
    }
    

    @Test
    @WithMockUser(authorities = {"caseworker-ia-legalrep-solicitor"})
    public void shouldHandleWhenNoInvolvementsFound(@WiremockResolver
        .Wiremock(factory = StaticPortWiremockFactory.class) WireMockServer server) throws Exception {

        final String homeOfficeReference = "1212-0099-0036-2016";
        addServiceAuthStub(server);
        addUserDetailsStub(server);
        addHomeOfficeAuthTokenStub(server);
        addHomeOfficeApiSearchNoInvolvementsStub(server, homeOfficeReference);
        addHomeOfficeApiInstructStub(server, homeOfficeReference);

        PreSubmitCallbackResponseForTest response = getPreSubmitCallbackResponse(homeOfficeReference);

        AsylumCase asylumCase = response.getAsylumCase();

        assertNotNull(response);
        assertNotNull(asylumCase);
        assertEquals(asylumCase.read(HOME_OFFICE_SEARCH_STATUS, String.class), Optional.of("FAIL"));
        final Optional<HomeOfficeCaseStatus> homeOfficeCaseStatus =
            asylumCase.read(HOME_OFFICE_CASE_STATUS_DATA, HomeOfficeCaseStatus.class);
        assertFalse(homeOfficeCaseStatus.isPresent());
    }

    @Test
    @WithMockUser(authorities = {"caseworker-ia-legalrep-solicitor"})
    public void shouldHandleWhenExtraFieldsAreSent(@WiremockResolver
        .Wiremock(factory = StaticPortWiremockFactory.class) WireMockServer server) throws Exception {

        final String homeOfficeReference = "extra-fields-ref-number";

        addServiceAuthStub(server);
        addUserDetailsStub(server);
        addHomeOfficeAuthTokenStub(server);
        addHomeOfficeApiSearchExtraFieldsStub(server, homeOfficeReference);
        addHomeOfficeApiInstructStub(server, homeOfficeReference);

        IaCaseHomeOfficeIntegrationApiClient iaCaseHomeOfficeIntegrationApiClient =
            new IaCaseHomeOfficeIntegrationApiClient(mockMvc);

        final CallbackForTestBuilder callback = callback()
            .event(Event.SUBMIT_APPEAL)
            .caseDetails(someCaseDetailsWith()
                .state(State.APPEAL_SUBMITTED)
                .caseData(anAsylumCase()
                    .with(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, APPEAL_REFERENCE_NUMBER)
                    .with(HOME_OFFICE_REFERENCE_NUMBER, homeOfficeReference)));


        PreSubmitCallbackResponseForTest response = iaCaseHomeOfficeIntegrationApiClient.aboutToSubmit(callback);

        AsylumCase asylumCase = response.getAsylumCase();

        assertNotNull(response);
        assertNotNull(asylumCase);
        assertEquals(asylumCase.read(HOME_OFFICE_SEARCH_STATUS, String.class), Optional.of("SUCCESS"));
        final Optional<HomeOfficeCaseStatus> homeOfficeCaseStatus =
            asylumCase.read(HOME_OFFICE_CASE_STATUS_DATA, HomeOfficeCaseStatus.class);
        assertTrue(homeOfficeCaseStatus.isPresent());
        if (homeOfficeCaseStatus.isPresent()) {
            Person person = homeOfficeCaseStatus.get().getPerson();
            assertNotNull(person);
            assertEquals(person.getGivenName(), "Capability");
            assertEquals(person.getFamilyName(), "Smith");
            assertEquals(person.getNationality().getDescription(), "Canada");
            assertEquals(person.getFullName(), "Capability Smith");
        }
    }


    @Test
    @WithMockUser(authorities = {"caseworker-ia-legalrep-solicitor"})
    public void shouldHandle400InternalSystemError(@WiremockResolver
        .Wiremock(factory = StaticPortWiremockFactory.class) WireMockServer server) throws Exception {

        final String homeOfficeReference = "1212-0099-0036-1000";
        addServiceAuthStub(server);
        addUserDetailsStub(server);
        addHomeOfficeAuthTokenStub(server);
        addHomeOfficeApiInstructStub(server, homeOfficeReference);
        addHomeOfficeApiSearch400InternalSystemErrorStub(server, homeOfficeReference);

        PreSubmitCallbackResponseForTest response = getPreSubmitCallbackResponse(homeOfficeReference);

        AsylumCase asylumCase = response.getAsylumCase();

        assertNotNull(response);
        assertNotNull(asylumCase);
        assertEquals(asylumCase.read(HOME_OFFICE_SEARCH_STATUS, String.class), Optional.of("FAIL"));
        final Optional<HomeOfficeCaseStatus> homeOfficeCaseStatus =
            asylumCase.read(HOME_OFFICE_CASE_STATUS_DATA, HomeOfficeCaseStatus.class);
        assertFalse(homeOfficeCaseStatus.isPresent());
    }

    @Test
    @WithMockUser(authorities = {"caseworker-ia-legalrep-solicitor"})
    public void shouldHandle400BadRequestError(@WiremockResolver
        .Wiremock(factory = StaticPortWiremockFactory.class) WireMockServer server) throws Exception {

        final String homeOfficeReference = "1212-0099-0036-XXXX";
        addServiceAuthStub(server);
        addUserDetailsStub(server);
        addHomeOfficeAuthTokenStub(server);
        addHomeOfficeApiInstructStub(server, homeOfficeReference);
        addHomeOfficeApiSearch400BadRequestStub(server, homeOfficeReference);

        PreSubmitCallbackResponseForTest response = getPreSubmitCallbackResponse(homeOfficeReference);

        AsylumCase asylumCase = response.getAsylumCase();

        assertNotNull(response);
        assertNotNull(asylumCase);
        assertEquals(asylumCase.read(HOME_OFFICE_SEARCH_STATUS, String.class), Optional.of("FAIL"));
        final Optional<HomeOfficeCaseStatus> homeOfficeCaseStatus =
            asylumCase.read(HOME_OFFICE_CASE_STATUS_DATA, HomeOfficeCaseStatus.class);
        assertFalse(homeOfficeCaseStatus.isPresent());
    }

    @Test
    @WithMockUser(authorities = {"caseworker-ia-legalrep-solicitor"})
    public void shouldHandle500ServerError(@WiremockResolver
        .Wiremock(factory = StaticPortWiremockFactory.class) WireMockServer server) throws Exception {

        final String homeOfficeReference = "1212-0099-0036-0500";
        addServiceAuthStub(server);
        addUserDetailsStub(server);
        addHomeOfficeAuthTokenStub(server);
        addHomeOfficeApiInstructStub(server, homeOfficeReference);
        addHomeOfficeApiSearch500ServerErrorStub(server, homeOfficeReference);

        Assertions.assertThatThrownBy(() -> {
            final PreSubmitCallbackResponseForTest preSubmitCallbackResponse
                = getPreSubmitCallbackResponse(homeOfficeReference);
            assertNull(preSubmitCallbackResponse);
        });
    }

    public PreSubmitCallbackResponseForTest getPreSubmitCallbackResponse(String homeOfficeReference) throws Exception {
        IaCaseHomeOfficeIntegrationApiClient iaCaseHomeOfficeIntegrationApiClient
            = new IaCaseHomeOfficeIntegrationApiClient(mockMvc);

        final CallbackForTestBuilder callback = callback()
            .event(Event.SUBMIT_APPEAL)
            .caseDetails(someCaseDetailsWith()
                .state(State.APPEAL_SUBMITTED)
                .caseData(anAsylumCase()
                    .with(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, APPEAL_REFERENCE_NUMBER)
                    .with(HOME_OFFICE_REFERENCE_NUMBER, homeOfficeReference)));

        return iaCaseHomeOfficeIntegrationApiClient.aboutToSubmit(callback);
    }
}
