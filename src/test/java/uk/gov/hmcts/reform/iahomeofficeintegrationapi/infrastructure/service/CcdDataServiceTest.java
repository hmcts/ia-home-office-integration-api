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
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.SubmitEventDetails;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.IdamService;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.CcdDataApi;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security.idam.IdentityManagerResponseException;

import java.time.LocalDateTime;
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
        testDto.setHoStatutoryTimeframeStatus(true);
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
        testDto.setHoStatutoryTimeframeStatus(true);
        testDto.setTimeStamp(LocalDateTime.of(2023, 12, 1, 10, 15, 30));

        // When
        List<IdValue<StatutoryTimeframe24Weeks>> result = ccdDataService.toStf4w("1", testDto);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        
        IdValue<StatutoryTimeframe24Weeks> idValue = result.get(0);
        assertEquals("1", idValue.getId());
        
        StatutoryTimeframe24Weeks value = idValue.getValue();
        assertEquals(YesOrNo.YES, value.getStatus());
        assertEquals("Home Office initial determination", value.getReason());
        assertEquals("Home Office Integration API", value.getUser());
        assertEquals("2023-12-01T10:15:30", value.getDateAdded());
    }

    @Test
    void shouldTestToStf4wMethodWithFalseStatus() {
        // Given
        testDto.setHoStatutoryTimeframeStatus(false);

        // When
        List<IdValue<StatutoryTimeframe24Weeks>> result = ccdDataService.toStf4w("2", testDto);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        
        IdValue<StatutoryTimeframe24Weeks> idValue = result.get(0);
        assertEquals("2", idValue.getId());
        
        StatutoryTimeframe24Weeks value = idValue.getValue();
        assertEquals(YesOrNo.NO, value.getStatus());
        assertEquals("Home Office initial determination", value.getReason());
        assertEquals("Home Office Integration API", value.getUser());
        assertEquals("2023-12-01T00:00:00", value.getDateAdded());
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
        testDto.setHoStatutoryTimeframeStatus(true);

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
        testDto.setHoStatutoryTimeframeStatus(false);

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
    void shouldLogCaseDetailsWhenPresent() {
        // Given
        String userToken = "test-user-token";
        testDto.setHoStatutoryTimeframeStatus(true);
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
        verify(mockCaseDetails).getCaseData();
    }

    @Test
    void shouldFormatDateTimeCorrectlyInToStf4wMethod() {
        testDto.setHoStatutoryTimeframeStatus(true);
        testDto.setTimeStamp(LocalDateTime.of(2024, 6, 15, 14, 28, 18));

        List<IdValue<StatutoryTimeframe24Weeks>> result = ccdDataService.toStf4w("3", testDto);

        assertNotNull(result);
        assertEquals(1, result.size());
        
        IdValue<StatutoryTimeframe24Weeks> idValue = result.get(0);
        StatutoryTimeframe24Weeks value = idValue.getValue();
        
        // Verify the datetime format preserves the time information (not just 00:00:00)
        assertEquals("2024-06-15T14:28:18", value.getDateAdded());
    }

}
