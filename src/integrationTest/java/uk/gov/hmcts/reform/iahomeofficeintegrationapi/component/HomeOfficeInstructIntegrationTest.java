package uk.gov.hmcts.reform.iahomeofficeintegrationapi.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.component.testutils.AsylumCaseForTest.anAsylumCase;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.component.testutils.CallbackForTest.CallbackForTestBuilder.callback;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.component.testutils.CaseDetailsForTest.CaseDetailsForTestBuilder.someCaseDetailsWith;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPEAL_SUBMISSION_DATE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPELLANT_DATE_OF_BIRTH;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.DIRECTIONS;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_INSTRUCT_STATUS;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;

import groovy.util.logging.Slf4j;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.component.testutils.CallbackForTest.CallbackForTestBuilder;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.component.testutils.IaCaseHomeOfficeIntegrationApiClient;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.component.testutils.PreSubmitCallbackResponseForTest;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.component.testutils.WithHomeOfficeAuthStub;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.component.testutils.WithHomeOfficeInstructStub;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.component.testutils.WithHomeOfficeStatusSearchStub;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.component.testutils.WithIdamStub;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.component.testutils.WithServiceAuthStub;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.Parties;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.IdValue;


@Slf4j
class HomeOfficeInstructIntegrationTest
    extends SpringBootIntegrationTest
    implements
        WithIdamStub, WithServiceAuthStub,
        WithHomeOfficeStatusSearchStub, WithHomeOfficeInstructStub, WithHomeOfficeAuthStub {

    public static final String APPEAL_REFERENCE_NUMBER = "some-appeal-reference-number";
    public static final String HOME_OFFICE_REFERENCE = "CustRef123";

    @Test
    @WithMockUser(authorities = {"caseworker-ia-caseofficer"})
    void shouldSendRequestEvidenceNotification() throws Exception {

        addServiceAuthStub(server);
        addUserInfoStub(server);
        addHomeOfficeAuthTokenStub(server);
        addHomeOfficeApiSearchValidResponseStub(server, HOME_OFFICE_REFERENCE);
        addHomeOfficeApiInstructStub(server, HOME_OFFICE_REFERENCE);

        IaCaseHomeOfficeIntegrationApiClient iaCaseHomeOfficeIntegrationApiClient
            = new IaCaseHomeOfficeIntegrationApiClient(mockMvc);

        final CallbackForTestBuilder callback = getCallbackResponse();


        PreSubmitCallbackResponseForTest response = iaCaseHomeOfficeIntegrationApiClient.aboutToSubmit(callback);

        AsylumCase asylumCase = response.getAsylumCase();
        assertEquals(Optional.of("OK"), asylumCase.read(HOME_OFFICE_INSTRUCT_STATUS, String.class));
    }

    private CallbackForTestBuilder getCallbackResponse() {
        return callback()
            .event(Event.REQUEST_RESPONDENT_EVIDENCE)
            .caseDetails(someCaseDetailsWith()
                .state(State.APPEAL_SUBMITTED)
                .caseData(anAsylumCase()
                    .with(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, APPEAL_REFERENCE_NUMBER)
                    .with(HOME_OFFICE_REFERENCE_NUMBER, HOME_OFFICE_REFERENCE)
                    .with(APPELLANT_GIVEN_NAMES, "GivenName")
                    .with(APPELLANT_FAMILY_NAME, "FamilyName")
                    .with(APPELLANT_DATE_OF_BIRTH, "1999-10-10")
                    .with(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, "DC/50001/2020")
                    .with(APPEAL_SUBMISSION_DATE, "2020-10-10")
                    .with(DIRECTIONS, getRequestEvidenceDirection())
                ));
    }

    @Test
    @WithMockUser(authorities = {"caseworker-ia-caseofficer"})
    void shouldHandle500ErrorResponse() throws Exception {

        addServiceAuthStub(server);
        addUserInfoStub(server);
        addHomeOfficeAuthTokenStub(server);
        addHomeOfficeApiSearchValidResponseStub(server, HOME_OFFICE_REFERENCE);
        addHomeOfficeApiInstruct500ServerErrorWithResponseStub(server, HOME_OFFICE_REFERENCE);

        IaCaseHomeOfficeIntegrationApiClient iaCaseHomeOfficeIntegrationApiClient
            = new IaCaseHomeOfficeIntegrationApiClient(mockMvc);

        final CallbackForTestBuilder callback = getCallbackResponse();


        PreSubmitCallbackResponseForTest response = iaCaseHomeOfficeIntegrationApiClient.aboutToSubmit(callback);

        assertNotNull(response);
        AsylumCase asylumCase = response.getAsylumCase();
        assertNotNull(asylumCase);
        assertEquals(asylumCase.read(HOME_OFFICE_INSTRUCT_STATUS, String.class), Optional.of("FAIL"));
    }

    @Test
    @WithMockUser(authorities = {"caseworker-ia-caseofficer"})
    void shouldHandle503ServiceUnavailableErrorResponse() throws Exception {

        addServiceAuthStub(server);
        addUserInfoStub(server);
        addHomeOfficeAuthTokenStub(server);
        addHomeOfficeApiSearchValidResponseStub(server, HOME_OFFICE_REFERENCE);
        addHomeOfficeApiInstruct503ServiceUnavailableErrorStub(server, HOME_OFFICE_REFERENCE);

        IaCaseHomeOfficeIntegrationApiClient iaCaseHomeOfficeIntegrationApiClient
            = new IaCaseHomeOfficeIntegrationApiClient(mockMvc);

        final CallbackForTestBuilder callback = getCallbackResponse();


        PreSubmitCallbackResponseForTest response = iaCaseHomeOfficeIntegrationApiClient.aboutToSubmit(callback);

        assertNotNull(response);
        AsylumCase asylumCase = response.getAsylumCase();
        assertNotNull(asylumCase);
        assertEquals(asylumCase.read(HOME_OFFICE_INSTRUCT_STATUS, String.class), Optional.of("FAIL"));
    }

    @Test
    @WithMockUser(authorities = {"caseworker-ia-caseofficer"})
    void shouldHandle500InternalServerErrorResponse() throws Exception {

        addServiceAuthStub(server);
        addUserInfoStub(server);
        addHomeOfficeAuthTokenStub(server);
        addHomeOfficeApiSearchValidResponseStub(server, HOME_OFFICE_REFERENCE);
        addHomeOfficeApiInstruct500InternalServerErrorStub(server, HOME_OFFICE_REFERENCE);

        IaCaseHomeOfficeIntegrationApiClient iaCaseHomeOfficeIntegrationApiClient
            = new IaCaseHomeOfficeIntegrationApiClient(mockMvc);

        final CallbackForTestBuilder callback = getCallbackResponse();


        PreSubmitCallbackResponseForTest response = iaCaseHomeOfficeIntegrationApiClient.aboutToSubmit(callback);

        assertNotNull(response);
        AsylumCase asylumCase = response.getAsylumCase();
        assertNotNull(asylumCase);
        assertEquals(asylumCase.read(HOME_OFFICE_INSTRUCT_STATUS, String.class), Optional.of("FAIL"));
    }

    @Test
    @WithMockUser(authorities = {"caseworker-ia-caseofficer"})
    void shouldHandle503ServiceUnavailableErrorResponseOnTokenRequest() throws Exception {

        addServiceAuthStub(server);
        addUserInfoStub(server);
        addHomeOfficeAuthToken503ServiceUnavailableStub(server);

        IaCaseHomeOfficeIntegrationApiClient iaCaseHomeOfficeIntegrationApiClient
            = new IaCaseHomeOfficeIntegrationApiClient(mockMvc);

        final CallbackForTestBuilder callback = getCallbackResponse();

        PreSubmitCallbackResponseForTest response = iaCaseHomeOfficeIntegrationApiClient.aboutToSubmit(callback);

        assertNotNull(response);
        AsylumCase asylumCase = response.getAsylumCase();
        assertNotNull(asylumCase);
        assertEquals(asylumCase.read(HOME_OFFICE_INSTRUCT_STATUS, String.class), Optional.of("FAIL"));
    }

    private Optional<List<IdValue<Direction>>> getRequestEvidenceDirection() {

        Direction direction = new Direction(
            "explanation", Parties.RESPONDENT, "dueDate", "dateSent", DirectionTag.RESPONDENT_EVIDENCE,
            Collections.emptyList());
        return
        Optional.of(
            Collections.singletonList(
                new IdValue<>("1", direction)
            )
        );
    }
}
