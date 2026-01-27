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

@ExtendWith(MockitoExtension.class)
class CcdDataServiceTest {

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
        testDto.setCcdCaseId(12345L);
        testDto.setStf24weeks(HomeOfficeStatutoryTimeframeDto.Stf24Weeks.builder()
            .status("Yes")
            .caseType("HU")
            .build());
        testDto.setTimeStamp(LocalDateTime.of(2023, 12, 1, 0, 0, 0));

        userInfo = new UserInfo("test@example.com", "test-uid", null, "Test User", "Test", "User");

        // Mock SubmitEventDetails with lenient stubbing to avoid unnecessary stubbing exceptions
        submitEventDetails = mock(SubmitEventDetails.class);
        lenient().when(submitEventDetails.getCallbackResponseStatusCode()).thenReturn(200);
        lenient().when(submitEventDetails.getCallbackResponseStatus()).thenReturn("Success");
    }

    @Test
    void shouldThrowIdentityManagerResponseExceptionWhenGetServiceUserTokenFails() throws Exception {
        // Given
        IdentityManagerResponseException expectedException = new IdentityManagerResponseException("Token generation failed", new RuntimeException());
        when(idamService.getServiceUserToken()).thenThrow(expectedException);

        // When & Then
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
        // Given
        when(idamService.getServiceUserToken()).thenReturn("test-user-token");
        RuntimeException s2sException = new RuntimeException("S2S token generation failed");
        when(serviceAuthorization.generate()).thenThrow(s2sException);

        // When & Then
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
        // Given
        testDto.setStf24weeks(HomeOfficeStatutoryTimeframeDto.Stf24Weeks.builder()
            .status("Yes")
            .caseType("HU")
            .build());
        testDto.setTimeStamp(LocalDateTime.of(2023, 12, 1, 10, 15, 30));

        // When
        StatutoryTimeframe24Weeks result = ccdDataService.toStf4w("1", testDto);

        // Then
        assertNotNull(result);
        assertEquals(YesOrNo.YES, result.getCurrentStatusAutoGenerated());
        
        // Verify history
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
        // Given
        testDto.setStf24weeks(HomeOfficeStatutoryTimeframeDto.Stf24Weeks.builder()
            .status("No")
            .caseType("HU")
            .build());

        // When
        StatutoryTimeframe24Weeks result = ccdDataService.toStf4w("2", testDto);

        // Then
        assertNotNull(result);
        assertEquals(YesOrNo.NO, result.getCurrentStatusAutoGenerated());
        
        // Verify history
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
        // Given
        String userToken = "test-user-token";
        String s2sToken = "test-s2s-token";

        when(idamService.getServiceUserToken()).thenReturn(userToken);
        when(serviceAuthorization.generate()).thenReturn(s2sToken);

        StartEventDetails mockStartEventDetails = mock(StartEventDetails.class);
        when(mockStartEventDetails.getCaseDetails()).thenReturn(null);

        when(ccdDataApi.startEventByCase(
            eq("Bearer test-user-token"),
            eq("test-s2s-token"),
            eq("12345"),
            eq(Event.SET_HOME_OFFICE_STATUTORY_TIMEFRAME_STATUS.toString())
        )).thenReturn(mockStartEventDetails);

        // When & Then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> ccdDataService.setHomeOfficeStatutoryTimeframeStatus(testDto)
        );

        assertEquals("Case details is null for caseId: 12345", exception.getMessage());
        
        verify(idamService).getServiceUserToken();
        verify(serviceAuthorization).generate();
        verify(ccdDataApi).startEventByCase(
            "Bearer " + userToken,
            s2sToken,
            "12345",
            Event.SET_HOME_OFFICE_STATUTORY_TIMEFRAME_STATUS.toString()
        );
    }

    @Test
    void shouldSuccessfullySetHomeOfficeStatutoryTimeframeStatusWithYesStatus() {
        // Given
        String userToken = "test-user-token";
        String s2sToken = "test-s2s-token";
        testDto.setStf24weeks(HomeOfficeStatutoryTimeframeDto.Stf24Weeks.builder()
            .status("Yes")
            .caseType("HU")
            .build());

        when(idamService.getServiceUserToken()).thenReturn(userToken);
        when(serviceAuthorization.generate()).thenReturn(s2sToken);

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
            eq("Bearer test-user-token"),
            eq("test-s2s-token"),
            eq("12345"),
            eq(Event.SET_HOME_OFFICE_STATUTORY_TIMEFRAME_STATUS.toString())
        )).thenReturn(mockStartEventDetails);

        SubmitEventDetails mockSubmitEventDetails = mock(SubmitEventDetails.class);
        when(mockSubmitEventDetails.getCallbackResponseStatusCode()).thenReturn(200);
        when(mockSubmitEventDetails.getCallbackResponseStatus()).thenReturn("Success");

        when(ccdDataApi.submitEventByCase(
            eq("Bearer test-user-token"),
            eq("test-s2s-token"),
            eq("12345"),
            any(CaseDataContent.class)
        )).thenReturn(mockSubmitEventDetails);

        // When
        SubmitEventDetails result = ccdDataService.setHomeOfficeStatutoryTimeframeStatus(testDto);

        // Then
        assertNotNull(result);
        assertEquals(200, result.getCallbackResponseStatusCode());
        assertEquals("Success", result.getCallbackResponseStatus());
        
        verify(idamService).getServiceUserToken();
        verify(serviceAuthorization).generate();
        verify(ccdDataApi).startEventByCase(
            "Bearer " + userToken,
            s2sToken,
            "12345",
            Event.SET_HOME_OFFICE_STATUTORY_TIMEFRAME_STATUS.toString()
        );
        verify(ccdDataApi).submitEventByCase(
            eq("Bearer " + userToken),
            eq(s2sToken),
            eq("12345"),
            any(CaseDataContent.class)
        );
    }

    @Test
    void shouldSuccessfullySetHomeOfficeStatutoryTimeframeStatusWithNoStatus() {
        // Given
        String userToken = "test-user-token";
        String s2sToken = "test-s2s-token";
        testDto.setStf24weeks(HomeOfficeStatutoryTimeframeDto.Stf24Weeks.builder()
            .status("No")
            .caseType("HU")
            .build());

        when(idamService.getServiceUserToken()).thenReturn(userToken);
        when(serviceAuthorization.generate()).thenReturn(s2sToken);

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
            eq("Bearer test-user-token"),
            eq("test-s2s-token"),
            eq("12345"),
            eq(Event.REMOVE_STATUTORY_TIMEFRAME_24_WEEKS.toString())
        )).thenReturn(mockStartEventDetails);

        SubmitEventDetails mockSubmitEventDetails = mock(SubmitEventDetails.class);
        when(mockSubmitEventDetails.getCallbackResponseStatusCode()).thenReturn(200);
        when(mockSubmitEventDetails.getCallbackResponseStatus()).thenReturn("Success");

        when(ccdDataApi.submitEventByCase(
            eq("Bearer test-user-token"),
            eq("test-s2s-token"),
            eq("12345"),
            any(CaseDataContent.class)
        )).thenReturn(mockSubmitEventDetails);

        // When
        SubmitEventDetails result = ccdDataService.setHomeOfficeStatutoryTimeframeStatus(testDto);

        // Then
        assertNotNull(result);
        assertEquals(200, result.getCallbackResponseStatusCode());
        assertEquals("Success", result.getCallbackResponseStatus());
        
        verify(idamService).getServiceUserToken();
        verify(serviceAuthorization).generate();
        verify(ccdDataApi).startEventByCase(
            "Bearer " + userToken,
            s2sToken,
            "12345",
            Event.REMOVE_STATUTORY_TIMEFRAME_24_WEEKS.toString()
        );
        verify(ccdDataApi).submitEventByCase(
            eq("Bearer " + userToken),
            eq(s2sToken),
            eq("12345"),
            any(CaseDataContent.class)
        );
    }

    @Test
    void shouldLogCaseDetailsWhenPresent() {
        // Given
        String userToken = "test-user-token";
        testDto.setStf24weeks(HomeOfficeStatutoryTimeframeDto.Stf24Weeks.builder()
            .status("Yes")
            .caseType("HU")
            .build());
        testDto.setTimeStamp(LocalDateTime.of(2024, 1, 15, 0, 0, 0));

        String s2sToken = "test-s2s-token";
        when(idamService.getServiceUserToken()).thenReturn(userToken);
        when(serviceAuthorization.generate()).thenReturn(s2sToken);

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
            eq("Bearer test-user-token"),
            eq("test-s2s-token"),
            eq("12345"),
            eq(Event.SET_HOME_OFFICE_STATUTORY_TIMEFRAME_STATUS.toString())
        )).thenReturn(mockStartEventDetails);

        SubmitEventDetails mockSubmitEventDetails = mock(SubmitEventDetails.class);
        when(mockSubmitEventDetails.getCallbackResponseStatusCode()).thenReturn(201);
        when(mockSubmitEventDetails.getCallbackResponseStatus()).thenReturn("Created");

        when(ccdDataApi.submitEventByCase(
            eq("Bearer test-user-token"),
            eq("test-s2s-token"),
            eq("12345"),
            any(CaseDataContent.class)
        )).thenReturn(mockSubmitEventDetails);

        // When
        SubmitEventDetails result = ccdDataService.setHomeOfficeStatutoryTimeframeStatus(testDto);

        // Then
        assertNotNull(result);
        assertEquals(201, result.getCallbackResponseStatusCode());
        assertEquals("Created", result.getCallbackResponseStatus());
        
        verify(mockCaseDetails).getId();
        verify(mockCaseDetails).getState();
        verify(mockCaseDetails).getCreatedDate();
        verify(mockCaseDetails).getCaseData(); // Now called only once
    }

    @Test
    void shouldFormatDateTimeCorrectlyInToStf4wMethod() {
        testDto.setStf24weeks(HomeOfficeStatutoryTimeframeDto.Stf24Weeks.builder()
            .status("Yes")
            .caseType("HU")
            .build());
        testDto.setTimeStamp(LocalDateTime.of(2024, 6, 15, 14, 28, 18));

        StatutoryTimeframe24Weeks result = ccdDataService.toStf4w("3", testDto);

        assertNotNull(result);
        
        // Verify the datetime format preserves the time information (not just 00:00:00)
        StatutoryTimeframe24WeeksHistory historyEntry = result.getHistory().get(0).getValue();
        assertEquals("2024-06-15T14:28:18", historyEntry.getDateTimeAdded());
    }

    @Test
    void shouldReturnTypeCompatibleWithStatutoryTimeframe24WeeksDefinition() {
        // Given
        testDto.setStf24weeks(HomeOfficeStatutoryTimeframeDto.Stf24Weeks.builder()
            .status("Yes")
            .caseType("HU")
            .build());
        testDto.setTimeStamp(LocalDateTime.of(2024, 1, 1, 12, 0, 0));

        // When
        StatutoryTimeframe24Weeks result = ccdDataService.toStf4w("1", testDto);

        // Then
        assertNotNull(result);
        
        // Verify the return type matches the TypeReference in AsylumCaseDefinition
        @SuppressWarnings("unchecked")
        TypeReference<StatutoryTimeframe24Weeks> expectedType = 
            (TypeReference<StatutoryTimeframe24Weeks>) AsylumCaseDefinition.STATUTORY_TIMEFRAME_24_WEEKS.getTypeReference();
        
        assertNotNull(expectedType);
        assertEquals(StatutoryTimeframe24Weeks.class, result.getClass(),
            "CCD def has probably changed but the method toStf4w has not been updated accordingly.");
        
        // Verify the structure has the expected properties
        assertNotNull(result.getCurrentStatusAutoGenerated());
        assertNotNull(result.getHistory());
    }

    @Test
    void nextHistoryId_shouldReturn1WhenNoExistingData() {
        // Given
        Optional<StatutoryTimeframe24Weeks> existingData = Optional.empty();

        // When
        String result = ccdDataService.nextHistoryId(existingData);

        // Then
        assertEquals("1", result);
    }

    @Test
    void nextHistoryId_shouldReturn1WhenHistoryIsNull() {
        // Given
        StatutoryTimeframe24Weeks data = mock(StatutoryTimeframe24Weeks.class);
        when(data.getHistory()).thenReturn(null);

        // When
        String result = ccdDataService.nextHistoryId(Optional.of(data));

        // Then
        assertEquals("1", result);
        verify(data).getHistory();
    }

    @Test
    void nextHistoryId_shouldReturn1WhenHistoryIsEmpty() {
        // Given
        StatutoryTimeframe24Weeks data = mock(StatutoryTimeframe24Weeks.class);
        when(data.getHistory()).thenReturn(new ArrayList<>());

        // When
        String result = ccdDataService.nextHistoryId(Optional.of(data));

        // Then
        assertEquals("1", result);
        verify(data).getHistory();
    }

    @Test
    void nextHistoryId_shouldReturn2WhenHistoryHasOneEntry() {
        // Given
        StatutoryTimeframe24Weeks data = mock(StatutoryTimeframe24Weeks.class);
        
        StatutoryTimeframe24WeeksHistory historyEntry = mock(StatutoryTimeframe24WeeksHistory.class);
        List<IdValue<StatutoryTimeframe24WeeksHistory>> historyList = new ArrayList<>();
        historyList.add(new IdValue<>("1", historyEntry));
        
        when(data.getHistory()).thenReturn(historyList);

        // When
        String result = ccdDataService.nextHistoryId(Optional.of(data));

        // Then
        assertEquals("2", result);
        verify(data).getHistory();
    }

    @Test
    void nextHistoryId_shouldReturn6WhenHistoryHasMultipleEntries() {
        // Given
        List<IdValue<StatutoryTimeframe24WeeksHistory>> historyList = new ArrayList<>();
        historyList.add(new IdValue<>("1", mock(StatutoryTimeframe24WeeksHistory.class)));
        historyList.add(new IdValue<>("3", mock(StatutoryTimeframe24WeeksHistory.class)));
        historyList.add(new IdValue<>("5", mock(StatutoryTimeframe24WeeksHistory.class)));
        
        StatutoryTimeframe24Weeks data = mock(StatutoryTimeframe24Weeks.class);
        when(data.getHistory()).thenReturn(historyList);

        // When
        String result = ccdDataService.nextHistoryId(Optional.of(data));

        // Then
        assertEquals("6", result);
        verify(data).getHistory();
    }

    @Test
    void nextHistoryId_shouldHandleNonSequentialIds() {
        // Given
        List<IdValue<StatutoryTimeframe24WeeksHistory>> historyList = new ArrayList<>();
        historyList.add(new IdValue<>("10", mock(StatutoryTimeframe24WeeksHistory.class)));
        historyList.add(new IdValue<>("2", mock(StatutoryTimeframe24WeeksHistory.class)));
        historyList.add(new IdValue<>("15", mock(StatutoryTimeframe24WeeksHistory.class)));
        
        StatutoryTimeframe24Weeks data = mock(StatutoryTimeframe24Weeks.class);
        when(data.getHistory()).thenReturn(historyList);

        // When
        String result = ccdDataService.nextHistoryId(Optional.of(data));

        // Then
        assertEquals("16", result);
        verify(data).getHistory();
    }

    @Test
    void shouldThrowExceptionWhenStatutoryTimeframeStatusAlreadySet() {
        // Given
        String userToken = "test-user-token";
        String s2sToken = "test-s2s-token";
        testDto.setStf24weeks(HomeOfficeStatutoryTimeframeDto.Stf24Weeks.builder()
            .status("Yes")
            .caseType("HU")
            .build());

        when(idamService.getServiceUserToken()).thenReturn(userToken);
        when(serviceAuthorization.generate()).thenReturn(s2sToken);

        // Create existing data with history
        StatutoryTimeframe24WeeksHistory existingHistoryEntry = mock(StatutoryTimeframe24WeeksHistory.class);
        List<IdValue<StatutoryTimeframe24WeeksHistory>> existingHistoryList = new ArrayList<>();
        existingHistoryList.add(new IdValue<>("1", existingHistoryEntry));

        StatutoryTimeframe24Weeks existingData = mock(StatutoryTimeframe24Weeks.class);
        when(existingData.getHistory()).thenReturn(existingHistoryList);
        when(existingData.getCurrentStatusAutoGenerated()).thenReturn(YesOrNo.NO);
        when(existingData.getCurrentHomeOfficeCaseTypeAutoGenerated()).thenReturn("EEA");

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
            eq("Bearer test-user-token"),
            eq("test-s2s-token"),
            eq("12345"),
            eq(Event.SET_HOME_OFFICE_STATUTORY_TIMEFRAME_STATUS.toString())
        )).thenReturn(mockStartEventDetails);

        // When & Then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> ccdDataService.setHomeOfficeStatutoryTimeframeStatus(testDto)
        );

        assertEquals(
            "Statutory timeframe status has already been set to 'No' for case type 'EEA' for caseId: 12345",
            exception.getMessage()
        );

        verify(idamService).getServiceUserToken();
        verify(serviceAuthorization).generate();
        verify(ccdDataApi).startEventByCase(
            "Bearer " + userToken,
            s2sToken,
            "12345",
            Event.SET_HOME_OFFICE_STATUTORY_TIMEFRAME_STATUS.toString()
        );
        verify(mockAsylumCase).read(AsylumCaseDefinition.STATUTORY_TIMEFRAME_24_WEEKS);
        verify(existingData).getHistory();
        verify(existingData).getCurrentStatusAutoGenerated();
        verify(existingData).getCurrentHomeOfficeCaseTypeAutoGenerated();
    }

    @Test
    void shouldThrowExceptionWhenStatusAlreadySetToYes() {
        // Given
        String userToken = "test-user-token";
        testDto.setCcdCaseId(99999L);
        testDto.setStf24weeks(HomeOfficeStatutoryTimeframeDto.Stf24Weeks.builder()
            .status("No")
            .caseType("PA")
            .build());

        String s2sToken = "test-s2s-token";
        when(idamService.getServiceUserToken()).thenReturn(userToken);
        when(serviceAuthorization.generate()).thenReturn(s2sToken);

        // Create existing data with multiple history entries
        List<IdValue<StatutoryTimeframe24WeeksHistory>> existingHistoryList = new ArrayList<>();
        existingHistoryList.add(new IdValue<>("1", mock(StatutoryTimeframe24WeeksHistory.class)));
        existingHistoryList.add(new IdValue<>("2", mock(StatutoryTimeframe24WeeksHistory.class)));

        StatutoryTimeframe24Weeks existingData = mock(StatutoryTimeframe24Weeks.class);
        when(existingData.getHistory()).thenReturn(existingHistoryList);
        when(existingData.getCurrentStatusAutoGenerated()).thenReturn(YesOrNo.YES);
        when(existingData.getCurrentHomeOfficeCaseTypeAutoGenerated()).thenReturn("HU");

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
            eq("Bearer test-user-token"),
            eq("test-s2s-token"),
            eq("99999"),
            eq(Event.REMOVE_STATUTORY_TIMEFRAME_24_WEEKS.toString())
        )).thenReturn(mockStartEventDetails);

        // When & Then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> ccdDataService.setHomeOfficeStatutoryTimeframeStatus(testDto)
        );

        assertEquals(
            "Statutory timeframe status has already been set to 'Yes' for case type 'HU' for caseId: 99999",
            exception.getMessage()
        );
        
        verify(idamService).getServiceUserToken();
        verify(serviceAuthorization).generate();
        verify(ccdDataApi).startEventByCase(
            "Bearer " + userToken,
            s2sToken,
            "99999",
            Event.REMOVE_STATUTORY_TIMEFRAME_24_WEEKS.toString()
        );
        verify(mockAsylumCase).read(AsylumCaseDefinition.STATUTORY_TIMEFRAME_24_WEEKS);
        verify(existingData).getHistory();
        verify(existingData).getCurrentStatusAutoGenerated();
        verify(existingData).getCurrentHomeOfficeCaseTypeAutoGenerated();
    }

    @Test
    void should_generate_s2s_token_successfully() {
        // Given
        String expectedToken = "test-s2s-token";
        when(serviceAuthorization.generate()).thenReturn(expectedToken);

        // When
        String actualToken = ccdDataService.generateS2SToken();

        // Then
        assertEquals(expectedToken, actualToken);
        verify(serviceAuthorization).generate();
    }

    @Test
    void should_call_auth_token_generator_when_generating_s2s_token() {
        // Given
        String token = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test";
        when(serviceAuthorization.generate()).thenReturn(token);

        // When
        ccdDataService.generateS2SToken();

        // Then
        verify(serviceAuthorization).generate();
    }

    @Test
    void shouldThrowCaseNotFoundExceptionWhenCaseIdIsNotValid() {
        // Given
        String userToken = "test-user-token";
        String s2sToken = "test-s2s-token";

        when(idamService.getServiceUserToken()).thenReturn(userToken);
        when(serviceAuthorization.generate()).thenReturn(s2sToken);

        when(ccdDataApi.startEventByCase(
            eq("Bearer test-user-token"),
            eq("test-s2s-token"),
            eq("12345"),
            eq(Event.SET_HOME_OFFICE_STATUTORY_TIMEFRAME_STATUS.toString())
        )).thenThrow(new HomeOfficeResponseException("StatusCode: 400, methodKey: CcdDataApi#startEventByCase, reason: null, message: Case ID is not valid"));

        // When & Then
        CaseNotFoundException exception = assertThrows(
            CaseNotFoundException.class,
            () -> ccdDataService.setHomeOfficeStatutoryTimeframeStatus(testDto)
        );

        assertEquals("Case not found for caseId: 12345", exception.getMessage());

        verify(idamService).getServiceUserToken();
        verify(serviceAuthorization).generate();
        verify(ccdDataApi).startEventByCase(
            "Bearer " + userToken,
            s2sToken,
            "12345",
            Event.SET_HOME_OFFICE_STATUTORY_TIMEFRAME_STATUS.toString()
        );
    }

    @Test
    void shouldRethrowOtherExceptionsFromStartEventByCase() {
        // Given
        String userToken = "test-user-token";
        String s2sToken = "test-s2s-token";

        when(idamService.getServiceUserToken()).thenReturn(userToken);
        when(serviceAuthorization.generate()).thenReturn(s2sToken);

        RuntimeException originalException = new RuntimeException("Some other error");
        when(ccdDataApi.startEventByCase(
            eq("Bearer test-user-token"),
            eq("test-s2s-token"),
            eq("12345"),
            eq(Event.SET_HOME_OFFICE_STATUTORY_TIMEFRAME_STATUS.toString())
        )).thenThrow(originalException);

        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> ccdDataService.setHomeOfficeStatutoryTimeframeStatus(testDto)
        );

        assertEquals("Some other error", exception.getMessage());
    }

}
