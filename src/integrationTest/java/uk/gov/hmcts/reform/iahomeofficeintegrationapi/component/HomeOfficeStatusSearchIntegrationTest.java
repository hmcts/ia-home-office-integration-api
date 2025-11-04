package uk.gov.hmcts.reform.iahomeofficeintegrationapi.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.component.testutils.AsylumCaseForTest.anAsylumCase;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.component.testutils.CallbackForTest.CallbackForTestBuilder.callback;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.component.testutils.CaseDetailsForTest.CaseDetailsForTestBuilder.someCaseDetailsWith;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_CASE_STATUS_DATA;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS;

import groovy.util.logging.Slf4j;
import java.util.Collections;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.component.testutils.CallbackForTest.CallbackForTestBuilder;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.component.testutils.IaCaseHomeOfficeIntegrationApiClient;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.component.testutils.PreSubmitCallbackResponseForTest;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.component.testutils.WithHomeOfficeAuthStub;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.component.testutils.WithHomeOfficeInstructStub;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.component.testutils.WithHomeOfficeStatusSearchStub;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.component.testutils.WithIdamStub;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.component.testutils.WithServiceAuthStub;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeCaseStatus;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.Person;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security.idam.IdamUserDetails;

@Slf4j
class HomeOfficeStatusSearchIntegrationTest
    extends SpringBootIntegrationTest
    implements
        WithIdamStub, WithServiceAuthStub,
        WithHomeOfficeStatusSearchStub, WithHomeOfficeInstructStub, WithHomeOfficeAuthStub {

    public static final String APPELLANT_FAMILY_NAME = "appellantFamilyName";
    public static final String APPELLANT_GIVEN_NAME = "appellantGivenName";
    public static final String APPELLANT_DATE_OF_BIRTH = "1970-1-21";
    public static final String APPEAL_REFERENCE_NUMBER = "some-appeal-reference-number";
    public static final String SUCCESS = "SUCCESS";
    public static final String FAIL = "FAIL";

    @MockBean
    private UserDetailsProvider userDetailsProvider;

    @Test
    @WithMockUser(authorities = {"caseworker-ia-legalrep-solicitor"})
    void shouldRetrieveHomeOfficeUserDetails() throws Exception {

        final String homeOfficeReference = "CustRef123";

        mockUserDetailsProvider();
        addServiceAuthStub(server);
        addUserInfoStub(server);
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
                    .with(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, APPELLANT_GIVEN_NAME)
                    .with(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, APPELLANT_FAMILY_NAME)
                    .with(AsylumCaseDefinition.APPELLANT_DATE_OF_BIRTH, APPELLANT_DATE_OF_BIRTH)
                    .with(HOME_OFFICE_REFERENCE_NUMBER, homeOfficeReference)));


        PreSubmitCallbackResponseForTest response = iaCaseHomeOfficeIntegrationApiClient.aboutToSubmit(callback);

        AsylumCase asylumCase = response.getAsylumCase();

        assertNotNull(response);
        assertNotNull(asylumCase);
        assertEquals(asylumCase.read(HOME_OFFICE_SEARCH_STATUS, String.class), Optional.of(SUCCESS));
        final Optional<HomeOfficeCaseStatus> homeOfficeCaseStatus
            = asylumCase.read(HOME_OFFICE_CASE_STATUS_DATA, HomeOfficeCaseStatus.class);
        assertTrue(homeOfficeCaseStatus.isPresent());
        Person person = homeOfficeCaseStatus.get().getPerson();
        assertNotNull(person);
        assertEquals(person.getGivenName(), "Capability");
        assertEquals(person.getFamilyName(), "Smith");
        assertEquals(person.getNationality().getDescription(), "Canada");
        assertEquals(person.getFullName(), "Capability Smith");
    }

    @Test
    @WithMockUser(authorities = {"caseworker-ia-legalrep-solicitor"})
    void shouldRetrieveHomeOfficeUserDetailsWithNullValue() throws Exception {

        final String homeOfficeReference = "CustRef000";

        mockUserDetailsProvider();
        addServiceAuthStub(server);
        addUserInfoStub(server);
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
                    .with(AsylumCaseDefinition.APPELLANT_DATE_OF_BIRTH, "1980-11-11")
                    .with(HOME_OFFICE_REFERENCE_NUMBER, homeOfficeReference)));


        PreSubmitCallbackResponseForTest response = iaCaseHomeOfficeIntegrationApiClient.aboutToSubmit(callback);

        AsylumCase asylumCase = response.getAsylumCase();

        assertNotNull(response);
        assertNotNull(asylumCase);
        assertEquals(asylumCase.read(HOME_OFFICE_SEARCH_STATUS, String.class), Optional.of(FAIL));
        final Optional<HomeOfficeCaseStatus> homeOfficeCaseStatus
            = asylumCase.read(HOME_OFFICE_CASE_STATUS_DATA, HomeOfficeCaseStatus.class);
        assertTrue(homeOfficeCaseStatus.isEmpty());
    }
    

    @Test
    @WithMockUser(authorities = {"caseworker-ia-legalrep-solicitor"})
    void shouldHandleWhenNoInvolvementsFound() throws Exception {

        final String homeOfficeReference = "1212-0099-0036-2016";

        mockUserDetailsProvider();
        addServiceAuthStub(server);
        addUserInfoStub(server);
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
    void shouldHandleWhenExtraFieldsAreSent() throws Exception {

        final String homeOfficeReference = "extra-fields-ref-number";

        mockUserDetailsProvider();
        addServiceAuthStub(server);
        addUserInfoStub(server);
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
                    .with(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, APPELLANT_GIVEN_NAME)
                    .with(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, APPELLANT_FAMILY_NAME)
                    .with(AsylumCaseDefinition.APPELLANT_DATE_OF_BIRTH, APPELLANT_DATE_OF_BIRTH)
                    .with(HOME_OFFICE_REFERENCE_NUMBER, homeOfficeReference)));


        PreSubmitCallbackResponseForTest response = iaCaseHomeOfficeIntegrationApiClient.aboutToSubmit(callback);

        AsylumCase asylumCase = response.getAsylumCase();

        assertNotNull(response);
        assertNotNull(asylumCase);
        assertEquals(asylumCase.read(HOME_OFFICE_SEARCH_STATUS, String.class), Optional.of(SUCCESS));
        final Optional<HomeOfficeCaseStatus> homeOfficeCaseStatus =
            asylumCase.read(HOME_OFFICE_CASE_STATUS_DATA, HomeOfficeCaseStatus.class);
        assertTrue(homeOfficeCaseStatus.isPresent());
        Person person = homeOfficeCaseStatus.get().getPerson();
        assertNotNull(person);
        assertEquals(person.getGivenName(), "Capability");
        assertEquals(person.getFamilyName(), "Smith");
        assertEquals(person.getNationality().getDescription(), "Canada");
        assertEquals(person.getFullName(), "Capability Smith");
    }


    @Test
    @WithMockUser(authorities = {"caseworker-ia-legalrep-solicitor"})
    void shouldHandle400InternalSystemError() throws Exception {

        final String homeOfficeReference = "1212-0099-0036-1000";

        mockUserDetailsProvider();
        addServiceAuthStub(server);
        addUserInfoStub(server);
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
    void shouldHandle400BadRequestError() throws Exception {

        final String homeOfficeReference = "1212-0099-0036-XXXX";

        mockUserDetailsProvider();
        addServiceAuthStub(server);
        addUserInfoStub(server);
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
    void shouldHandle500ServerError() throws Exception {

        final String homeOfficeReference = "1212-0099-0036-0500";

        mockUserDetailsProvider();
        addServiceAuthStub(server);
        addUserInfoStub(server);
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
                    .with(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, APPELLANT_GIVEN_NAME)
                    .with(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, APPELLANT_FAMILY_NAME)
                    .with(AsylumCaseDefinition.APPELLANT_DATE_OF_BIRTH, APPELLANT_DATE_OF_BIRTH)
                    .with(HOME_OFFICE_REFERENCE_NUMBER, homeOfficeReference)));

        return iaCaseHomeOfficeIntegrationApiClient.aboutToSubmit(callback);
    }

    private void mockUserDetailsProvider() {
        UserDetails userDetails = new IdamUserDetails(
                "Bearer eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2lkIjoiYi9PNk92VnYxK3krV2dySDV",
                "75211309-2318-451a-8cd3-00cdccb4be76",
                Collections.singletonList("ia-caseworker"),
                "some@email.com",
                "some forename",
                "some surname");
        BDDMockito.given(userDetailsProvider.getUserDetails()).willReturn(userDetails);
    }
}
