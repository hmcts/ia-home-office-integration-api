package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.service;

import com.fasterxml.jackson.core.type.TypeReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.CaseGoneException;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.CaseNotFoundException;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeStatutoryTimeframeDto;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.CaseDataContent;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.StartEventDetails;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.StatutoryTimeframe24Weeks;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.StatutoryTimeframe24WeeksHistory;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.SubmitEventDetails;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.IdamService;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.DbUtils;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.CcdDataApi;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.HomeOfficeResponseException;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security.idam.IdentityManagerResponseException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CcdDataServiceTest {

    private static final String HMCTS_REF_NUM = "PA/12345/2026";
    private static final String CCD_CASE_ID = "1773833350220320";

    private static final String USER_TOKEN = "Bearer test-user-token";
    private static final String S2S_TOKEN = "Bearer test-s2s-token";

    private static final String COHORT_NAME = "HU";

    @Mock
    private CcdDataApi ccdDataApi;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator serviceAuthorization;

    @Mock
    private DbUtils dbUtils;

    @InjectMocks
    private CcdDataService ccdDataService;

    private HomeOfficeStatutoryTimeframeDto testDto;

    @BeforeEach
    void setUp() {

        testDto = new HomeOfficeStatutoryTimeframeDto();
        testDto.setHmctsReferenceNumber(HMCTS_REF_NUM);
        testDto.setTimeStamp(
            OffsetDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC)
        );

        setIncluded(true);
    }

    @Test
    void shouldThrowIdentityManagerResponseExceptionWhenGetServiceUserTokenFails() {

        when(idamService.getServiceUserToken())
            .thenThrow(
                new IdentityManagerResponseException(
                    "Token generation failed",
                    new RuntimeException()
                )
            );

        IdentityManagerResponseException exception = assertThrows(
            IdentityManagerResponseException.class,
            () -> ccdDataService.setHomeOfficeStatutoryTimeframeStatus(testDto)
        );

        assertEquals("Token generation failed", exception.getMessage());

        verifyNoInteractions(serviceAuthorization);
        verifyNoInteractions(ccdDataApi);
    }

    @Test
    void shouldThrowWhenS2STokenGenerationFails() {

        when(idamService.getServiceUserToken())
            .thenReturn(USER_TOKEN);

        when(serviceAuthorization.generate())
            .thenThrow(new RuntimeException("S2S token generation failed"));

        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> ccdDataService.setHomeOfficeStatutoryTimeframeStatus(testDto)
        );

        assertEquals("S2S token generation failed", exception.getMessage());
    }

    @ParameterizedTest
    @CsvSource({
        "YES,1,2024-01-01T12:13:14Z",
        "NO,2,2024-01-01T12:00:00Z"
    })
    void shouldBuildStf24w(
        YesOrNo status,
        String historyId,
        String expectedDate
    ) {

        testDto.setTimeStamp(
            OffsetDateTime.parse(expectedDate)
        );

        StatutoryTimeframe24Weeks result =
            ccdDataService.toStf24w(historyId, status, testDto);

        assertNotNull(result);
        assertEquals(1, result.getHistory().size());
        assertEquals(historyId, result.getHistory().getFirst().getId());

        StatutoryTimeframe24WeeksHistory history =
            result.getHistory().getFirst().getValue();

        assertEquals(status, history.getStatus());
        assertEquals(expectedDate, history.getDateTimeAdded());
        assertEquals(
            "Home Office initial determination",
            history.getReason()
        );
        assertEquals(
            "Home Office Integration API",
            history.getUser()
        );
    }

    @Test
    void shouldReturnTypeCompatibleWithStatutoryTimeframe24WeeksDefinition() {

        StatutoryTimeframe24Weeks result =
            ccdDataService.toStf24w("1", YesOrNo.YES, testDto);

        @SuppressWarnings("unchecked")
        TypeReference<StatutoryTimeframe24Weeks> expectedType =
            (TypeReference<StatutoryTimeframe24Weeks>)
                AsylumCaseDefinition.STATUTORY_TIMEFRAME_24_WEEKS
                    .getTypeReference();

        assertNotNull(expectedType);
        assertNotNull(AsylumCaseDefinition.STATUTORY_TIMEFRAME_24_WEEKS.getTypeReference());
        assertNotNull(result.getHistory());
    }

    @Test
    void shouldThrowWhenCaseDetailsIsNull() {

        stubCaseId();
        stubTokens();

        StartEventDetails start = mock(StartEventDetails.class);

        when(start.getCaseDetails()).thenReturn(null);

        when(ccdDataApi.startEventByCase(
            eq(USER_TOKEN),
            eq(S2S_TOKEN),
            eq(CCD_CASE_ID),
            anyString()
        )).thenReturn(start);

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> ccdDataService.setHomeOfficeStatutoryTimeframeStatus(testDto)
        );

        assertTrue(
            exception.getMessage().startsWith("Case details is null")
        );
    }

    @Test
    void shouldSuccessfullySetStatusYes() {

        stubCaseId();
        stubTokens();
        stubHappyPath();

        SubmitEventDetails result =
            ccdDataService.setHomeOfficeStatutoryTimeframeStatus(testDto);

        assertNotNull(result);
        assertEquals(200, result.getCallbackResponseStatusCode());

        verify(ccdDataApi).submitEventByCase(
            eq(USER_TOKEN),
            eq(S2S_TOKEN),
            eq(CCD_CASE_ID),
            any(CaseDataContent.class)
        );
    }

    @Test
    void shouldSuccessfullySetStatusNo() {

        setIncluded(false);

        stubCaseId();
        stubTokens();
        stubHappyPath();

        SubmitEventDetails result =
            ccdDataService.setHomeOfficeStatutoryTimeframeStatus(testDto);

        assertNotNull(result);
        assertEquals(200, result.getCallbackResponseStatusCode());

        verify(ccdDataApi).submitEventByCase(
            eq(USER_TOKEN),
            eq(S2S_TOKEN),
            eq(CCD_CASE_ID),
            any(CaseDataContent.class)
        );
    }

    @ParameterizedTest
    @MethodSource("historyIds")
    void nextHistoryId_shouldReturnExpectedValue(
        List<IdValue<StatutoryTimeframe24WeeksHistory>> history,
        String expected
    ) {

        StatutoryTimeframe24Weeks data =
            mock(StatutoryTimeframe24Weeks.class);

        when(data.getHistory()).thenReturn(history);

        assertEquals(
            expected,
            ccdDataService.nextHistoryId(Optional.of(data))
        );
    }

    static Stream<Arguments> historyIds() {

        return Stream.of(
            Arguments.of(null, "1"),
            Arguments.of(List.of(), "1"),
            Arguments.of(history("1"), "2"),
            Arguments.of(history("1", "3", "5"), "6"),
            Arguments.of(history("10", "2", "15"), "16")
        );
    }

    @Test
    void shouldThrowWhenStatusAlreadySet() {

        stubCaseId();
        stubTokens();

        AsylumCase asylumCase = mock(AsylumCase.class);

        StatutoryTimeframe24Weeks existing =
            mock(StatutoryTimeframe24Weeks.class);

        when(existing.getHistory()).thenReturn(history("1"));

        when(asylumCase.read(any()))
            .thenReturn(Optional.of(existing));

        stubStartEvent(asylumCase);

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> ccdDataService.setHomeOfficeStatutoryTimeframeStatus(testDto)
        );

        assertEquals(
            "Statutory timeframe status has already been set for caseId: " + CCD_CASE_ID,
            exception.getMessage()
        );
    }

    @Test
    void shouldThrowCaseNotFoundException() {

        when(dbUtils.getCaseId(
            HMCTS_REF_NUM
        )).thenThrow(
            new IllegalStateException("Case ID could not be found from appeal reference number " + HMCTS_REF_NUM + ".")
        );

        assertThrows(
            CaseNotFoundException.class,
            () -> ccdDataService.setHomeOfficeStatutoryTimeframeStatus(testDto)
        );
    }

    @Test
    void shouldThrowCaseGoneException() {

        stubDefaultDependencies();

        when(ccdDataApi.startEventByCase(
            eq(USER_TOKEN),
            eq(S2S_TOKEN),
            eq(CCD_CASE_ID),
            anyString()
        )).thenThrow(
            new HomeOfficeResponseException("Case ID is not valid")
        );

        assertThrows(
            CaseGoneException.class,
            () -> ccdDataService.setHomeOfficeStatutoryTimeframeStatus(testDto)
        );
    }

    @Test
    void shouldRethrowOtherExceptions() {

        when(idamService.getServiceUserToken())
            .thenReturn(USER_TOKEN);

        when(serviceAuthorization.generate())
            .thenReturn(S2S_TOKEN);

        when(ccdDataApi.startEventByCase(
            eq(USER_TOKEN),
            eq(S2S_TOKEN),
            nullable(String.class),
            eq(Event.SET_HOME_OFFICE_STATUTORY_TIMEFRAME_STATUS.toString())
        )).thenThrow(new RuntimeException("Some other error"));

        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> ccdDataService.setHomeOfficeStatutoryTimeframeStatus(testDto)
        );

        assertEquals("Some other error", exception.getMessage());

        verify(ccdDataApi).startEventByCase(
            eq(USER_TOKEN),
            eq(S2S_TOKEN),
            nullable(String.class),
            eq(Event.SET_HOME_OFFICE_STATUTORY_TIMEFRAME_STATUS.toString())
        );
    }

    @ParameterizedTest
    @CsvSource({
        "test-user-token,Bearer test-s2s-token",
        "'Bearer Bearer test-user-token',Bearer test-s2s-token",
        "Bearer test-user-token,test-s2s-token",
        "Bearer test-user-token,'Bearer Bearer test-s2s-token'"
    })
    void shouldNormaliseTokens(
        String userToken,
        String s2sToken
    ) {

        stubCaseId();

        when(idamService.getServiceUserToken()).thenReturn(userToken);
        when(serviceAuthorization.generate()).thenReturn(s2sToken);

        stubHappyPath();

        ccdDataService.setHomeOfficeStatutoryTimeframeStatus(testDto);

        verify(ccdDataApi).startEventByCase(
            eq(USER_TOKEN),
            eq(S2S_TOKEN),
            anyString(),
            anyString()
        );
    }

    @ParameterizedTest
    @CsvSource({
        "' '",
        "''"
    })
    void shouldThrowWhenUserTokenInvalid(String token) {

        when(idamService.getServiceUserToken()).thenReturn(token);

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> ccdDataService.setHomeOfficeStatutoryTimeframeStatus(testDto)
        );

        assertEquals("Token is null or blank", exception.getMessage());

        verifyNoInteractions(ccdDataApi);
    }

    @Test
    void shouldGenerateS2SToken() {

        when(serviceAuthorization.generate())
            .thenReturn("test-s2s-token");

        assertEquals(
            "test-s2s-token",
            ccdDataService.generateS2SToken()
        );

        verify(serviceAuthorization).generate();
    }

    @Test
    void shouldReturnServiceUserToken() {

        when(idamService.getServiceUserToken())
            .thenReturn(USER_TOKEN);

        assertEquals(
            USER_TOKEN,
            ccdDataService.getServiceUserToken()
        );

        verify(idamService).getServiceUserToken();
    }

    private void setIncluded(boolean included) {

        testDto.setStf24weekCohortDtos(
            List.of(
                HomeOfficeStatutoryTimeframeDto.Stf24WeekCohortDto.builder()
                    .name(COHORT_NAME)
                    .included(included)
                    .build()
            )
        );
    }

    private void stubCaseId() {

        when(dbUtils.getCaseId(HMCTS_REF_NUM))
            .thenReturn(CCD_CASE_ID);
    }

    private void stubTokens() {

        when(idamService.getServiceUserToken())
            .thenReturn(USER_TOKEN);

        when(serviceAuthorization.generate())
            .thenReturn(S2S_TOKEN);
    }

    private void stubHappyPath() {

        AsylumCase asylumCase = mock(AsylumCase.class);

        when(asylumCase.read(any()))
            .thenReturn(Optional.empty());

        stubStartEvent(asylumCase);

        SubmitEventDetails submit = mock(SubmitEventDetails.class);

        when(submit.getCallbackResponseStatusCode())
            .thenReturn(200);

        when(ccdDataApi.submitEventByCase(
            anyString(),
            anyString(),
            nullable(String.class),
            any(CaseDataContent.class)
        )).thenReturn(submit);
    }

    private void stubStartEvent(AsylumCase asylumCase) {

        @SuppressWarnings("unchecked")
        CaseDetails<AsylumCase> caseDetails = mock(CaseDetails.class);

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        StartEventDetails start = mock(StartEventDetails.class);

        when(start.getCaseDetails()).thenReturn(caseDetails);

        when(ccdDataApi.startEventByCase(
            anyString(),
            anyString(),
            nullable(String.class),
            anyString()
        )).thenReturn(start);
    }

    private static List<IdValue<StatutoryTimeframe24WeeksHistory>> history(
        String... ids
    ) {

        return Arrays.stream(ids)
            .map(id ->
                new IdValue<>(
                    id,
                    mock(StatutoryTimeframe24WeeksHistory.class)
                )
            )
            .toList();
    }

    private void stubDefaultDependencies() {

        when(dbUtils.getCaseId(HMCTS_REF_NUM))
            .thenReturn(CCD_CASE_ID);

        when(idamService.getServiceUserToken())
            .thenReturn(USER_TOKEN);

        when(serviceAuthorization.generate())
            .thenReturn(S2S_TOKEN);
    }
}