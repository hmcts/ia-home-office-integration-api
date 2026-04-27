package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeStatutoryTimeframeDto;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.model.idam.UserInfo;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.CaseDataContent;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.StartEventDetails;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.StatutoryTimeframe24Weeks;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.StatutoryTimeframe24WeeksHistory;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.SubmitEventDetails;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.IdamService;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.CaseNotFoundException;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.CcdDataApi;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.HomeOfficeResponseException;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security.idam.IdentityManagerResponseException;
import com.fasterxml.jackson.core.type.TypeReference;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.IdValue;

import java.util.HashMap;
import java.util.Optional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.STF_24W_PREVIOUS_STATUS_WAS_YES_AUTO_GENERATED;

@ExtendWith(MockitoExtension.class)
class CcdDataServiceTest {

    public static final String CCD_CASE_ID = "12345";
    private static final String USER_TOKEN = "Bearer test-user-token";
    private static final String S2S_TOKEN = "Bearer test-s2s-token";

    @Mock
    private CcdDataApi ccdDataApi;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator serviceAuthorization;

    @InjectMocks
    private CcdDataService ccdDataService;

    private HomeOfficeStatutoryTimeframeDto testDto;
    private UserInfo userInfo;
    private StartEventDetails startEventDetails;
    private SubmitEventDetails submitEventDetails;

    @BeforeEach
    void setUp() {
        testDto = new HomeOfficeStatutoryTimeframeDto();
        testDto.setCcdCaseId(CCD_CASE_ID);
        testDto.setStf24weeks(HomeOfficeStatutoryTimeframeDto.Stf24Weeks.builder()
            .status("Yes")
            .cohorts(new String[]{"HU"})
            .build());
        testDto.setTimeStamp(LocalDateTime.of(2023, 12, 1, 0, 0, 0));

        userInfo = new UserInfo("test@example.com", "test-uid", null, "Test User", "Test", "User");

        submitEventDetails = mock(SubmitEventDetails.class);
        lenient().when(submitEventDetails.getCallbackResponseStatusCode()).thenReturn(200);
        lenient().when(submitEventDetails.getCallbackResponseStatus()).thenReturn("Success");
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenUserTokenIsMissingBearerPrefix() {
        when(idamService.getServiceUserToken()).thenReturn("no-bearer-token");

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> ccdDataService.setHomeOfficeStatutoryTimeframeStatus(testDto)
        );

        assertEquals("user token is missing 'Bearer' prefix: no-bearer-token", exception.getMessage());
        verifyNoInteractions(ccdDataApi);
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenUserTokenHasMultipleBearerPrefixes() {
        when(idamService.getServiceUserToken()).thenReturn("Bearer Bearer test-user-token");

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> ccdDataService.setHomeOfficeStatutoryTimeframeStatus(testDto)
        );

        assertEquals("user token has multiple 'Bearer' prefixes: Bearer Bearer test-user-token", exception.getMessage());
        verifyNoInteractions(ccdDataApi);
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenS2STokenIsMissingBearerPrefix() {
        when(idamService.getServiceUserToken()).thenReturn(USER_TOKEN);
        when(serviceAuthorization.generate()).thenReturn("no-bearer-s2s-token");

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> ccdDataService.setHomeOfficeStatutoryTimeframeStatus(testDto)
        );

        assertEquals("S2S token is missing 'Bearer' prefix: no-bearer-s2s-token", exception.getMessage());
        verifyNoInteractions(ccdDataApi);
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenS2STokenHasMultipleBearerPrefixes() {
        when(idamService.getServiceUserToken()).thenReturn(USER_TOKEN);
        when(serviceAuthorization.generate()).thenReturn("Bearer Bearer test-s2s-token");

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> ccdDataService.setHomeOfficeStatutoryTimeframeStatus(testDto)
        );

        assertEquals("S2S token has multiple 'Bearer' prefixes: Bearer Bearer test-s2s-token", exception.getMessage());
        verifyNoInteractions(ccdDataApi);
    }

    @Test
    void shouldThrowIdentityManagerResponseExceptionWhenGetServiceUserTokenFails() throws Exception {
        IdentityManagerResponseException expectedException = new IdentityManagerResponseException("Token generation failed", new RuntimeException());
        when(idamService.getServiceUserToken()).thenThrow(expectedException);

        IdentityManagerResponseException exception = assertThrows(
            IdentityManagerResponseException.class,
            () -> ccdDataService.setHomeOfficeStatutoryTimeframeStatus(testDto)
        );

        assertEquals("Token generation failed", exception.getMessage());
        verify(idamService).getServiceUserToken();
        verifyNoInteractions(serviceAuthorization);
        verifyNoInteractions(ccdDataApi);
    }

    @Test
    void shouldThrowIdentityManagerResponseExceptionWhenS2STokenGenerationFails() throws Exception {
        when(idamService.getServiceUserToken()).thenReturn(USER_TOKEN);
        RuntimeException s2sException = new RuntimeException("S2S token generation failed");
        when(serviceAuthorization.generate()).thenThrow(s2sException);

        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> ccdDataService.setHomeOfficeStatutoryTimeframeStatus(testDto)
        );

        assertEquals("S2S token generation failed", exception.getMessage());
        verify(idamService).getServiceUserToken();
        verify(serviceAuthorization).generate();
        verifyNoInteractions(ccdDataApi);
    }

    @Test
    void shouldTestToStf4wMethodWithTrueStatus() {
        testDto.setStf24weeks(HomeOfficeStatutoryTimeframeDto.Stf24Weeks.builder()
            .status("Yes")
            .cohorts(new String[]{"HU"})
            .build());
        testDto.setTimeStamp(LocalDateTime.of(2023, 12, 1, 10, 15, 30));

        StatutoryTimeframe24Weeks result = ccdDataService.toStf4w("1", testDto);

        assertNotNull(result);
        assertNotNull(result.getHistory());
        assertEquals(1, result.getHistory().size());

        StatutoryTimeframe24WeeksHistory historyEntry = result.getHistory().get(0).getValue();
        assertEquals(YesOrNo.YES, historyEntry.getStatus());
        assertEquals("Home Office initial determination", historyEntry.getReason());
        assertEquals("Home Office Integration API", historyEntry.getUser());
        assertEquals("2023-12-01T10:15:30", historyEntry.getDateTimeAdded());
    }

    @Test
    void shouldTestToStf4wMethodWithFalseStatus() {
        testDto.setStf24weeks(HomeOfficeStatutoryTimeframeDto.Stf24Weeks.builder()
            .status("No")
            .cohorts(new String[]{"HU"})
            .build());

        StatutoryTimeframe24Weeks result = ccdDataService.toStf4w("2", testDto);

        assertNotNull(result);
        assertNotNull(result.getHistory());
        assertEquals(1, result.getHistory().size());

        StatutoryTimeframe24WeeksHistory historyEntry = result.getHistory().get(0).getValue();
        assertEquals(YesOrNo.NO, historyEntry.getStatus());
        assertEquals("Home Office initial determination", historyEntry.getReason());
        assertEquals("Home Office Integration API", historyEntry.getUser());
        assertEquals("2023-12-01T00:00:00", historyEntry.getDateTimeAdded());
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenCaseDetailsIsNull() {
        when(idamService.getServiceUserToken()).thenReturn(USER_TOKEN);
        when(serviceAuthorization.generate()).thenReturn(S2S_TOKEN);

        StartEventDetails mockStartEventDetails = mock(StartEventDetails.class);
        when(mockStartEventDetails.getCaseDetails()).thenReturn(null);

        when(ccdDataApi.startEventByCase(
            eq(USER_TOKEN),
            eq(S2S_TOKEN),
            eq(CCD_CASE_ID),
            eq(Event.SET_HOME_OFFICE_STATUTORY_TIMEFRAME_STATUS.toString())
        )).thenReturn(mockStartEventDetails);

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> ccdDataService.setHomeOfficeStatutoryTimeframeStatus(testDto)
        );

        assertEquals("Case details is null for caseId: 12345", exception.getMessage());
        verify(idamService).getServiceUserToken();
        verify(serviceAuthorization).generate();
        verify(ccdDataApi).startEventByCase(USER_TOKEN, S2S_TOKEN, CCD_CASE_ID,
            Event.SET_HOME_OFFICE_STATUTORY_TIMEFRAME_STATUS.toString());
    }

    @Test
    void shouldSuccessfullySetHomeOfficeStatutoryTimeframeStatusWithYesStatus() {
        testDto.setStf24weeks(HomeOfficeStatutoryTimeframeDto.Stf24Weeks.builder()
            .status("Yes")
            .cohorts(new String[]{"HU"})
            .build());

        when(idamService.getServiceUserToken()).thenReturn(USER_TOKEN);
        when(serviceAuthorization.generate()).thenReturn(S2S_TOKEN);

        StartEventDetails mockStartEventDetails = mock(StartEventDetails.class);
        @SuppressWarnings("unchecked")
        CaseDetails<AsylumCase> mockCaseDetails = mock(CaseDetails.class);
        when(mockCaseDetails.getId()).thenReturn(12345L);
        when(mockCaseDetails.getState()).thenReturn(State.APPEAL_SUBMITTED);
        when(mockCaseDetails.getCreatedDate()).thenReturn(java.time.LocalDateTime.now());
        when(mockCaseDetails.getCaseData()).thenReturn(mock(AsylumCase.class));

        when(mockStartEventDetails.getCaseDetails()).thenReturn(mockCaseDetails);
        when(mockStartEventDetails.getToken()).thenReturn("test-event-token");
        when(mockStartEventDetails.getEventId()).thenReturn(Event.SET_HOME_OFFICE_STATUTORY_TIMEFRAME_STATUS);

        when(ccdDataApi.startEventByCase(
            eq(USER_TOKEN), eq(S2S_TOKEN), eq(CCD_CASE_ID),
            eq(Event.SET_HOME_OFFICE_STATUTORY_TIMEFRAME_STATUS.toString())
        )).thenReturn(mockStartEventDetails);

        SubmitEventDetails mockSubmitEventDetails = mock(SubmitEventDetails.class);
        when(mockSubmitEventDetails.getCallbackResponseStatusCode()).thenReturn(200);
        when(mockSubmitEventDetails.getCallbackResponseStatus()).thenReturn("Success");

        HashMap<String, Object> data = new HashMap<>();
        data.put(STF_24W_PREVIOUS_STATUS_WAS_YES_AUTO_GENERATED.value(), YesOrNo.YES);
        when(mockSubmitEventDetails.getData()).thenReturn(data);

        when(ccdDataApi.submitEventByCase(
            eq(USER_TOKEN), eq(S2S_TOKEN), eq(CCD_CASE_ID), any(CaseDataContent.class)
        )).thenReturn(mockSubmitEventDetails);

        SubmitEventDetails result = ccdDataService.setHomeOfficeStatutoryTimeframeStatus(testDto);

        assertNotNull(result);
        assertEquals(200, result.getCallbackResponseStatusCode());
        assertEquals("Success", result.getCallbackResponseStatus());
        assertEquals(YesOrNo.YES, result.getData().get(STF_24W_PREVIOUS_STATUS_WAS_YES_AUTO_GENERATED.value()));

        verify(idamService).getServiceUserToken();
        verify(serviceAuthorization).generate();
        verify(ccdDataApi).startEventByCase(USER_TOKEN, S2S_TOKEN, CCD_CASE_ID,
            Event.SET_HOME_OFFICE_STATUTORY_TIMEFRAME_STATUS.toString());
        verify(ccdDataApi).submitEventByCase(eq(USER_TOKEN), eq(S2S_TOKEN), eq(CCD_CASE_ID), any(CaseDataContent.class));
    }

    @Test
    void shouldSuccessfullySetHomeOfficeStatutoryTimeframeStatusWithNoStatus() {
        testDto.setStf24weeks(HomeOfficeStatutoryTimeframeDto.Stf24Weeks.builder()
            .status("No")
            .cohorts(new String[]{"HU"})
            .build());

        when(idamService.getServiceUserToken()).thenReturn(USER_TOKEN);
        when(serviceAuthorization.generate()).thenReturn(S2S_TOKEN);

        StartEventDetails mockStartEventDetails = mock(StartEventDetails.class);
        @SuppressWarnings("unchecked")
        CaseDetails<AsylumCase> mockCaseDetails = mock(CaseDetails.class);
        when(mockCaseDetails.getId()).thenReturn(12345L);
        when(mockCaseDetails.getState()).thenReturn(State.APPEAL_SUBMITTED);
        when(mockCaseDetails.getCreatedDate()).thenReturn(java.time.LocalDateTime.now());
        when(mockCaseDetails.getCaseData()).thenReturn(mock(AsylumCase.class));

        when(mockStartEventDetails.getCaseDetails()).thenReturn(mockCaseDetails);
        when(mockStartEventDetails.getToken()).thenReturn("test-event-token");
        when(mockStartEventDetails.getEventId()).thenReturn(Event.REMOVE_STATUTORY_TIMEFRAME_24_WEEKS);

        when(ccdDataApi.startEventByCase(
            eq(USER_TOKEN), eq(S2S_TOKEN), eq(CCD_CASE_ID),
            eq(Event.REMOVE_STATUTORY_TIMEFRAME_24_WEEKS.toString())
        )).thenReturn(mockStartEventDetails);

        SubmitEventDetails mockSubmitEventDetails = mock(SubmitEventDetails.class);
        when(mockSubmitEventDetails.getCallbackResponseStatusCode()).thenReturn(200);
        when(mockSubmitEventDetails.getCallbackResponseStatus()).thenReturn("Success");

        HashMap<String, Object> data = new HashMap<>();
        data.put(STF_24W_PREVIOUS_STATUS_WAS_YES_AUTO_GENERATED.value(), YesOrNo.NO);
        when(mockSubmitEventDetails.getData()).thenReturn(data);

        when(ccdDataApi.submitEventByCase(
            eq(USER_TOKEN), eq(S2S_TOKEN), eq(CCD_CASE_ID), any(CaseDataContent.class)
        )).thenReturn(mockSubmitEventDetails);

        SubmitEventDetails result = ccdDataService.setHomeOfficeStatutoryTimeframeStatus(testDto);

        assertNotNull(result);
        assertEquals(200, result.getCallbackResponseStatusCode());
        assertEquals("Success", result.getCallbackResponseStatus());
        assertEquals(YesOrNo.NO, result.getData().get(STF_24W_PREVIOUS_STATUS_WAS_YES_AUTO_GENERATED.value()));

        verify(idamService).getServiceUserToken();
        verify(serviceAuthorization).generate();
        verify(ccdDataApi).startEventByCase(USER_TOKEN, S2S_TOKEN, CCD_CASE_ID,
            Event.REMOVE_STATUTORY_TIMEFRAME_24_WEEKS.toString());
        verify(ccdDataApi).submitEventByCase(eq(USER_TOKEN), eq(S2S_TOKEN), eq(CCD_CASE_ID), any(CaseDataContent.class));
    }

    @Test
    void shouldLogCaseDetailsWhenPresent() {
        testDto.setStf24weeks(HomeOfficeStatutoryTimeframeDto.Stf24Weeks.builder()
            .status("Yes")
            .cohorts(new String[]{"HU"})
            .build());
        testDto.setTimeStamp(LocalDateTime.of(2024, 1, 15, 0, 0, 0));

        when(idamService.getServiceUserToken()).thenReturn(USER_TOKEN);
        when(serviceAuthorization.generate()).thenReturn(S2S_TOKEN);

        StartEventDetails mockStartEventDetails = mock(StartEventDetails.class);
        @SuppressWarnings("unchecked")
        CaseDetails<AsylumCase> mockCaseDetails = mock(CaseDetails.class);
        java.time.LocalDateTime createdDate = java.time.LocalDateTime.of(2024, 1, 1, 10, 0);

        when(mockCaseDetails.getId()).thenReturn(67890L);
        when(mockCaseDetails.getState()).thenReturn(State.APPEAL_STARTED);
        when(mockCaseDetails.getCreatedDate()).thenReturn(createdDate);
        when(mockCaseDetails.getCaseData()).thenReturn(mock(AsylumCase.class));

        when(mockStartEventDetails.getCaseDetails()).thenReturn(mockCaseDetails);
        when(mockStartEventDetails.getToken()).thenReturn("test-event-token-2");
        when(mockStartEventDetails.getEventId()).thenReturn(Event.SET_HOME_OFFICE_STATUTORY_TIMEFRAME_STATUS);

        when(ccdDataApi.startEventByCase(
            eq(USER_TOKEN), eq(S2S_TOKEN), eq(CCD_CASE_ID),
            eq(Event.SET_HOME_OFFICE_STATUTORY_TIMEFRAME_STATUS.toString())
        )).thenReturn(mockStartEventDetails);

        SubmitEventDetails mockSubmitEventDetails = mock(SubmitEventDetails.class);
        when(mockSubmitEventDetails.getCallbackResponseStatusCode()).thenReturn(201);
        when(mockSubmitEventDetails.getCallbackResponseStatus()).thenReturn("Created");

        when(ccdDataApi.submitEventByCase(
            eq(USER_TOKEN), eq(S2S_TOKEN), eq(CCD_CASE_ID), any(CaseDataContent.class)
        )).thenReturn(mockSubmitEventDetails);

        SubmitEventDetails result = ccdDataService.setHomeOfficeStatutoryTimeframeStatus(testDto);

        assertNotNull(result);
        assertEquals(201, result.getCallbackResponseStatusCode());
        assertEquals("Created", result.getCallbackResponseStatus());

        verify(mockCaseDetails).getId();
        verify(mockCaseDetails).getState();
        verify(mockCaseDetails).getCreatedDate();
        verify(mockCaseDetails).getCaseData();
    }

    @Test
    void shouldFormatDateTimeCorrectlyInToStf4wMethod() {
        testDto.setStf24weeks(HomeOfficeStatutoryTimeframeDto.Stf24Weeks.builder()
            .status("Yes")
            .cohorts(new String[]{"HU"})
            .build());
        testDto.setTimeStamp(LocalDateTime.of(2024, 6, 15, 14, 28, 18));

        StatutoryTimeframe24Weeks result = ccdDataService.toStf4w("3", testDto);

        assertNotNull(result);
        StatutoryTimeframe24WeeksHistory historyEntry = result.getHistory().get(0).getValue();
        assertEquals("2024-06-15T14:28:18", historyEntry.getDateTimeAdded());
    }

    @Test
    void shouldReturnTypeCompatibleWithStatutoryTimeframe24WeeksDefinition() {
        testDto.setStf24weeks(HomeOfficeStatutoryTimeframeDto.Stf24Weeks.builder()
            .status("Yes")
            .cohorts(new String[]{"HU"})
            .build());
        testDto.setTimeStamp(LocalDateTime.of(2024, 1, 1, 12, 0, 0));

        StatutoryTimeframe24Weeks result = ccdDataService.toStf4w("1", testDto);

        assertNotNull(result);
        @SuppressWarnings("unchecked")
        TypeReference<StatutoryTimeframe24Weeks> expectedType =
            (TypeReference<StatutoryTimeframe24Weeks>) AsylumCaseDefinition.STATUTORY_TIMEFRAME_24_WEEKS.getTypeReference();
        assertNotNull(expectedType);
        assertEquals(StatutoryTimeframe24Weeks.class, result.getClass(),
            "CCD def has probably changed but the method toStf4w has not been updated accordingly.");
        assertNotNull(result.getHistory());
    }

    @Test
    void nextHistoryId_shouldReturn1WhenNoExistingData() {
        assertEquals("1", ccdDataService.nextHistoryId(Optional.empty()));
    }

    @Test
    void nextHistoryId_shouldReturn1WhenHistoryIsNull() {
        StatutoryTimeframe24Weeks data = mock(StatutoryTimeframe24Weeks.class);
        when(data.getHistory()).thenReturn(null);

        assertEquals("1", ccdDataService.nextHistoryId(Optional.of(data)));
        verify(data).getHistory();
    }

    @Test
    void nextHistoryId_shouldReturn1WhenHistoryIsEmpty() {
        StatutoryTimeframe24Weeks data = mock(StatutoryTimeframe24Weeks.class);
        when(data.getHistory()).thenReturn(new ArrayList<>());

        assertEquals("1", ccdDataService.nextHistoryId(Optional.of(data)));
        verify(data).getHistory();
    }

    @Test
    void nextHistoryId_shouldReturn2WhenHistoryHasOneEntry() {
        StatutoryTimeframe24Weeks data = mock(StatutoryTimeframe24Weeks.class);
        List<IdValue<StatutoryTimeframe24WeeksHistory>> historyList = new ArrayList<>();
        historyList.add(new IdValue<>("1", mock(StatutoryTimeframe24WeeksHistory.class)));
        when(data.getHistory()).thenReturn(historyList);

        assertEquals("2", ccdDataService.nextHistoryId(Optional.of(data)));
        verify(data).getHistory();
    }

    @Test
    void nextHistoryId_shouldReturn6WhenHistoryHasMultipleEntries() {
        List<IdValue<StatutoryTimeframe24WeeksHistory>> historyList = new ArrayList<>();
        historyList.add(new IdValue<>("1", mock(StatutoryTimeframe24WeeksHistory.class)));
        historyList.add(new IdValue<>("3", mock(StatutoryTimeframe24WeeksHistory.class)));
        historyList.add(new IdValue<>("5", mock(StatutoryTimeframe24WeeksHistory.class)));

        StatutoryTimeframe24Weeks data = mock(StatutoryTimeframe24Weeks.class);
        when(data.getHistory()).thenReturn(historyList);

        assertEquals("6", ccdDataService.nextHistoryId(Optional.of(data)));
        verify(data).getHistory();
    }

    @Test
    void nextHistoryId_shouldHandleNonSequentialIds() {
        List<IdValue<StatutoryTimeframe24WeeksHistory>> historyList = new ArrayList<>();
        historyList.add(new IdValue<>("10", mock(StatutoryTimeframe24WeeksHistory.class)));
        historyList.add(new IdValue<>("2", mock(StatutoryTimeframe24WeeksHistory.class)));
        historyList.add(new IdValue<>("15", mock(StatutoryTimeframe24WeeksHistory.class)));

        StatutoryTimeframe24Weeks data = mock(StatutoryTimeframe24Weeks.class);
        when(data.getHistory()).thenReturn(historyList);

        assertEquals("16", ccdDataService.nextHistoryId(Optional.of(data)));
        verify(data).getHistory();
    }

    @Test
    void shouldThrowExceptionWhenStatutoryTimeframeStatusAlreadySet() {
        when(idamService.getServiceUserToken()).thenReturn(USER_TOKEN);
        when(serviceAuthorization.generate()).thenReturn(S2S_TOKEN);

        StatutoryTimeframe24WeeksHistory existingHistoryEntry = mock(StatutoryTimeframe24WeeksHistory.class);
        List<IdValue<StatutoryTimeframe24WeeksHistory>> existingHistoryList = new ArrayList<>();
        existingHistoryList.add(new IdValue<>("1", existingHistoryEntry));

        StatutoryTimeframe24Weeks existingData = mock(StatutoryTimeframe24Weeks.class);
        when(existingData.getHistory()).thenReturn(existingHistoryList);

        AsylumCase mockAsylumCase = mock(AsylumCase.class);
        when(mockAsylumCase.read(AsylumCaseDefinition.STATUTORY_TIMEFRAME_24_WEEKS))
            .thenReturn(Optional.of(existingData));

        StartEventDetails mockStartEventDetails = mock(StartEventDetails.class);
        @SuppressWarnings("unchecked")
        CaseDetails<AsylumCase> mockCaseDetails = mock(CaseDetails.class);
        when(mockCaseDetails.getId()).thenReturn(12345L);
        when(mockCaseDetails.getState()).thenReturn(State.APPEAL_SUBMITTED);
        when(mockCaseDetails.getCreatedDate()).thenReturn(java.time.LocalDateTime.now());
        when(mockCaseDetails.getCaseData()).thenReturn(mockAsylumCase);

        when(mockStartEventDetails.getCaseDetails()).thenReturn(mockCaseDetails);
        lenient().when(mockStartEventDetails.getToken()).thenReturn("test-event-token");
        lenient().when(mockStartEventDetails.getEventId()).thenReturn(Event.SET_HOME_OFFICE_STATUTORY_TIMEFRAME_STATUS);

        when(ccdDataApi.startEventByCase(
            eq(USER_TOKEN), eq(S2S_TOKEN), eq(CCD_CASE_ID),
            eq(Event.SET_HOME_OFFICE_STATUTORY_TIMEFRAME_STATUS.toString())
        )).thenReturn(mockStartEventDetails);

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> ccdDataService.setHomeOfficeStatutoryTimeframeStatus(testDto)
        );

        assertEquals("Statutory timeframe status has already been set for caseId: 12345", exception.getMessage());

        verify(idamService).getServiceUserToken();
        verify(serviceAuthorization).generate();
        verify(ccdDataApi).startEventByCase(USER_TOKEN, S2S_TOKEN, CCD_CASE_ID,
            Event.SET_HOME_OFFICE_STATUTORY_TIMEFRAME_STATUS.toString());
        verify(mockAsylumCase).read(AsylumCaseDefinition.STATUTORY_TIMEFRAME_24_WEEKS);
        verify(existingData).getHistory();
    }

    @Test
    void shouldThrowExceptionWhenStatusAlreadySetToYes() {
        testDto.setCcdCaseId("99999");
        testDto.setStf24weeks(HomeOfficeStatutoryTimeframeDto.Stf24Weeks.builder()
            .status("No")
            .cohorts(new String[]{"HU"})
            .build());

        when(idamService.getServiceUserToken()).thenReturn(USER_TOKEN);
        when(serviceAuthorization.generate()).thenReturn(S2S_TOKEN);

        List<IdValue<StatutoryTimeframe24WeeksHistory>> existingHistoryList = new ArrayList<>();
        existingHistoryList.add(new IdValue<>("1", mock(StatutoryTimeframe24WeeksHistory.class)));
        existingHistoryList.add(new IdValue<>("2", mock(StatutoryTimeframe24WeeksHistory.class)));

        StatutoryTimeframe24Weeks existingData = mock(StatutoryTimeframe24Weeks.class);
        when(existingData.getHistory()).thenReturn(existingHistoryList);

        AsylumCase mockAsylumCase = mock(AsylumCase.class);
        when(mockAsylumCase.read(AsylumCaseDefinition.STATUTORY_TIMEFRAME_24_WEEKS))
            .thenReturn(Optional.of(existingData));

        StartEventDetails mockStartEventDetails = mock(StartEventDetails.class);
        @SuppressWarnings("unchecked")
        CaseDetails<AsylumCase> mockCaseDetails = mock(CaseDetails.class);
        when(mockCaseDetails.getId()).thenReturn(99999L);
        when(mockCaseDetails.getState()).thenReturn(State.APPEAL_SUBMITTED);
        when(mockCaseDetails.getCreatedDate()).thenReturn(java.time.LocalDateTime.now());
        when(mockCaseDetails.getCaseData()).thenReturn(mockAsylumCase);

        when(mockStartEventDetails.getCaseDetails()).thenReturn(mockCaseDetails);
        lenient().when(mockStartEventDetails.getToken()).thenReturn("test-event-token");
        lenient().when(mockStartEventDetails.getEventId()).thenReturn(Event.REMOVE_STATUTORY_TIMEFRAME_24_WEEKS);

        when(ccdDataApi.startEventByCase(
            eq(USER_TOKEN), eq(S2S_TOKEN), eq("99999"),
            eq(Event.REMOVE_STATUTORY_TIMEFRAME_24_WEEKS.toString())
        )).thenReturn(mockStartEventDetails);

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> ccdDataService.setHomeOfficeStatutoryTimeframeStatus(testDto)
        );

        assertEquals("Statutory timeframe status has already been set for caseId: 99999", exception.getMessage());

        verify(idamService).getServiceUserToken();
        verify(serviceAuthorization).generate();
        verify(ccdDataApi).startEventByCase(USER_TOKEN, S2S_TOKEN, "99999",
            Event.REMOVE_STATUTORY_TIMEFRAME_24_WEEKS.toString());
        verify(mockAsylumCase).read(AsylumCaseDefinition.STATUTORY_TIMEFRAME_24_WEEKS);
        verify(existingData).getHistory();
    }

    @Test
    void should_generate_s2s_token_successfully() {
        String expectedToken = "test-s2s-token";
        when(serviceAuthorization.generate()).thenReturn(expectedToken);

        String actualToken = ccdDataService.generateS2SToken();

        assertEquals(expectedToken, actualToken);
        verify(serviceAuthorization).generate();
    }

    @Test
    void should_call_auth_token_generator_when_generating_s2s_token() {
        when(serviceAuthorization.generate()).thenReturn("Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test");

        ccdDataService.generateS2SToken();

        verify(serviceAuthorization).generate();
    }

    @Test
    void shouldThrowCaseNotFoundExceptionWhenCaseIdIsNotValid() {
        when(idamService.getServiceUserToken()).thenReturn(USER_TOKEN);
        when(serviceAuthorization.generate()).thenReturn(S2S_TOKEN);

        when(ccdDataApi.startEventByCase(
            eq(USER_TOKEN), eq(S2S_TOKEN), eq("12345"),
            eq(Event.SET_HOME_OFFICE_STATUTORY_TIMEFRAME_STATUS.toString())
        )).thenThrow(new HomeOfficeResponseException(
            "StatusCode: 400, methodKey: CcdDataApi#startEventByCase, reason: null, message: Case ID is not valid"));

        CaseNotFoundException exception = assertThrows(
            CaseNotFoundException.class,
            () -> ccdDataService.setHomeOfficeStatutoryTimeframeStatus(testDto)
        );

        assertEquals("Case not found for caseId: 12345", exception.getMessage());

        verify(idamService).getServiceUserToken();
        verify(serviceAuthorization).generate();
        verify(ccdDataApi).startEventByCase(USER_TOKEN, S2S_TOKEN, "12345",
            Event.SET_HOME_OFFICE_STATUTORY_TIMEFRAME_STATUS.toString());
    }

    @Test
    void shouldRethrowOtherExceptionsFromStartEventByCase() {
        when(idamService.getServiceUserToken()).thenReturn(USER_TOKEN);
        when(serviceAuthorization.generate()).thenReturn(S2S_TOKEN);

        RuntimeException originalException = new RuntimeException("Some other error");
        when(ccdDataApi.startEventByCase(
            eq(USER_TOKEN), eq(S2S_TOKEN), eq("12345"),
            eq(Event.SET_HOME_OFFICE_STATUTORY_TIMEFRAME_STATUS.toString())
        )).thenThrow(originalException);

        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> ccdDataService.setHomeOfficeStatutoryTimeframeStatus(testDto)
        );

        assertEquals("Some other error", exception.getMessage());
    }
}
