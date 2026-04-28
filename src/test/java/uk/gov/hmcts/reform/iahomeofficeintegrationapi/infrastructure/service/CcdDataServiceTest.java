package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.STATUTORY_TIMEFRAME_24_WEEKS;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.STF_24W_PREVIOUS_STATUS_WAS_YES_AUTO_GENERATED;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.CaseNotFoundException;

import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeStatutoryTimeframeDto;

import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.StartEventDetails;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.StatutoryTimeframe24Weeks;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.StatutoryTimeframe24WeeksHistory;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.SubmitEventDetails;

import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.YesOrNo;

import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.IdamService;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.DbUtils;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.CcdDataApi;

import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security.idam.IdentityManagerResponseException;

@ExtendWith(MockitoExtension.class)
class CcdDataServiceTest {

    private static final String CASE_ID = "1773047655725509";
    private static final String HMCTS_REF_NUM = "PA/12345/2026";

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

    private HomeOfficeStatutoryTimeframeDto dto;

    @BeforeEach
    void setup() {

        dto = new HomeOfficeStatutoryTimeframeDto();
        dto.setHmctsReferenceNumber(HMCTS_REF_NUM);

        dto.setStf24weekCohorts(List.of(
            HomeOfficeStatutoryTimeframeDto.Stf24WeekCohort.builder()
                .name("HU")
                .included("true")
                .build()
        ));

        dto.setTimeStamp(
            OffsetDateTime.of(2024,1,1,12,0,0,0, ZoneOffset.UTC)
        );
    }

    private StartEventDetails buildStartEvent(AsylumCase asylumCase, Event event) {

        StartEventDetails start = mock(StartEventDetails.class);

        @SuppressWarnings("unchecked")
        CaseDetails<AsylumCase> caseDetails = mock(CaseDetails.class);

        when(caseDetails.getId()).thenReturn(Long.valueOf(CASE_ID));
        when(caseDetails.getState()).thenReturn(State.APPEAL_SUBMITTED);
        when(caseDetails.getCreatedDate()).thenReturn(LocalDateTime.now());
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(start.getCaseDetails()).thenReturn(caseDetails);
        when(start.getEventId()).thenReturn(event);

        return start;
    }

    private SubmitEventDetails buildSubmitResponse() {

        SubmitEventDetails response = mock(SubmitEventDetails.class);

        when(response.getCallbackResponseStatusCode()).thenReturn(200);
        when(response.getCallbackResponseStatus()).thenReturn("Success");

        Map<String,Object> data = new HashMap<>();
        data.put(STF_24W_PREVIOUS_STATUS_WAS_YES_AUTO_GENERATED.value(), YesOrNo.YES);

        return response;
    }

    @Test
    void shouldThrowIdentityManagerExceptionWhenUserTokenFails() {

        when(idamService.getServiceUserToken())
            .thenThrow(new IdentityManagerResponseException("fail", new RuntimeException()));

        assertThrows(
            IdentityManagerResponseException.class,
            () -> ccdDataService.setHomeOfficeStatutoryTimeframeStatus(dto)
        );

        verify(idamService).getServiceUserToken();
        verifyNoInteractions(serviceAuthorization);
        verifyNoInteractions(ccdDataApi);
    }

    @Test
    void shouldThrowIdentityManagerResponseExceptionWhenS2STokenGenerationFails() throws Exception {
        // Given
        when(idamService.getServiceUserToken()).thenReturn("Bearer test-user-token");
        RuntimeException s2sException = new RuntimeException("S2S token generation failed");
        when(serviceAuthorization.generate()).thenThrow(s2sException);

        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> ccdDataService.setHomeOfficeStatutoryTimeframeStatus(dto)
        );

        assertEquals("boom", ex.getMessage());
    }

    @Test
    void shouldThrowIllegalStateWhenCaseDetailsNull() {

        when(idamService.getServiceUserToken()).thenReturn("token");
        when(serviceAuthorization.generate()).thenReturn("s2s");
        when(dbUtils.getCaseId(HMCTS_REF_NUM)).thenReturn(CASE_ID);

        StartEventDetails start = mock(StartEventDetails.class);
        when(start.getCaseDetails()).thenReturn(null);

        when(ccdDataApi.startEventByCase(any(), any(), eq(CASE_ID), any()))
            .thenReturn(start);

    @Test
    void shouldThrowIllegalStateExceptionWhenCaseDetailsIsNull() {
        // Given
        String userToken = "test-user-token";
        String s2sToken = "Bearer test-s2s-token";

        when(idamService.getServiceUserToken()).thenReturn("Bearer " + userToken);
        when(serviceAuthorization.generate()).thenReturn(s2sToken);

        StartEventDetails mockStartEventDetails = mock(StartEventDetails.class);
        when(mockStartEventDetails.getCaseDetails()).thenReturn(null);

        when(ccdDataApi.startEventByCase(
            eq("Bearer test-user-token"),
            eq("Bearer test-s2s-token"),
            eq(CCD_CASE_ID),
            eq(Event.SET_HOME_OFFICE_STATUTORY_TIMEFRAME_STATUS.toString())
        )).thenReturn(mockStartEventDetails);

        // When & Then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> ccdDataService.setHomeOfficeStatutoryTimeframeStatus(dto)
        );

        assertTrue(ex.getMessage().contains("Case details is null"));
    }

    @Test
    void shouldSuccessfullySetHomeOfficeStatutoryTimeframeStatusWithYesStatus() {
        // Given
        String userToken = "test-user-token";
        String s2sToken = "Bearer test-s2s-token";
        testDto.setStf24weeks(HomeOfficeStatutoryTimeframeDto.Stf24Weeks.builder()
            .status("Yes")
            .cohorts(new String[]{"HU"})
            .build());

        when(idamService.getServiceUserToken()).thenReturn("Bearer " + userToken);
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
            eq("Bearer test-s2s-token"),
            eq(CCD_CASE_ID),
            eq(Event.SET_HOME_OFFICE_STATUTORY_TIMEFRAME_STATUS.toString())
        )).thenReturn(mockStartEventDetails);

        SubmitEventDetails mockSubmitEventDetails = mock(SubmitEventDetails.class);
        when(mockSubmitEventDetails.getCallbackResponseStatusCode()).thenReturn(200);
        when(mockSubmitEventDetails.getCallbackResponseStatus()).thenReturn("Success");

        HashMap<String, Object> data = new HashMap<>();
        data.put(STF_24W_PREVIOUS_STATUS_WAS_YES_AUTO_GENERATED.value(), YesOrNo.YES);
        when(mockSubmitEventDetails.getData()).thenReturn(data);

        when(ccdDataApi.submitEventByCase(
            eq("Bearer test-user-token"),
            eq("Bearer test-s2s-token"),
            eq(CCD_CASE_ID),
            any(CaseDataContent.class)
        )).thenReturn(mockSubmitEventDetails);

        when(idamService.getServiceUserToken()).thenReturn("user");
        when(serviceAuthorization.generate()).thenReturn("s2s");
        when(dbUtils.getCaseId(HMCTS_REF_NUM)).thenReturn(CASE_ID);

        AsylumCase asylumCase = mock(AsylumCase.class);
        when(asylumCase.read(STATUTORY_TIMEFRAME_24_WEEKS))
            .thenReturn(Optional.empty());

        StartEventDetails start = buildStartEvent(
            asylumCase,
            Event.SET_HOME_OFFICE_STATUTORY_TIMEFRAME_STATUS
        );

    @Test
    void shouldSuccessfullySetHomeOfficeStatutoryTimeframeStatusWithNoStatus() {
        // Given
        String userToken = "test-user-token";
        String s2sToken = "Bearer test-s2s-token";
        testDto.setStf24weeks(HomeOfficeStatutoryTimeframeDto.Stf24Weeks.builder()
            .status("No")
            .cohorts(new String[]{"HU"})
            .build());

        when(idamService.getServiceUserToken()).thenReturn("Bearer " + userToken);
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
            eq("Bearer test-s2s-token"),
            eq(CCD_CASE_ID),
            eq(Event.REMOVE_STATUTORY_TIMEFRAME_24_WEEKS.toString())
        )).thenReturn(mockStartEventDetails);

        SubmitEventDetails mockSubmitEventDetails = mock(SubmitEventDetails.class);
        when(mockSubmitEventDetails.getCallbackResponseStatusCode()).thenReturn(200);
        when(mockSubmitEventDetails.getCallbackResponseStatus()).thenReturn("Success");

        HashMap<String, Object> data = new HashMap<>();
        data.put(STF_24W_PREVIOUS_STATUS_WAS_YES_AUTO_GENERATED.value(), YesOrNo.NO);
        when(mockSubmitEventDetails.getData()).thenReturn(data);

        when(ccdDataApi.submitEventByCase(
            eq("Bearer test-user-token"),
            eq("Bearer test-s2s-token"),
            eq(CCD_CASE_ID),
            any(CaseDataContent.class)
        )).thenReturn(mockSubmitEventDetails);

        // When
        SubmitEventDetails result = ccdDataService.setHomeOfficeStatutoryTimeframeStatus(testDto);

        // Then
        assertNotNull(result);
        assertEquals(200, result.getCallbackResponseStatusCode());
        assertEquals("Success", result.getCallbackResponseStatus());
        assertEquals(YesOrNo.NO, result.getData().get(STF_24W_PREVIOUS_STATUS_WAS_YES_AUTO_GENERATED.value()));
        
        verify(idamService).getServiceUserToken();
        verify(serviceAuthorization).generate();
        verify(ccdDataApi).startEventByCase(
            "Bearer " + userToken,
            s2sToken,
                CCD_CASE_ID,
            Event.REMOVE_STATUTORY_TIMEFRAME_24_WEEKS.toString()
        );
        verify(ccdDataApi).submitEventByCase(
            eq("Bearer " + userToken),
            eq(s2sToken),
            eq(CCD_CASE_ID),
            any(CaseDataContent.class)
        );
    }

    @Test
    void shouldLogCaseDetailsWhenPresent() {
        // Given
        String userToken = "test-user-token";
        testDto.setStf24weeks(HomeOfficeStatutoryTimeframeDto.Stf24Weeks.builder()
            .status("Yes")
            .cohorts(new String[]{"HU"})
            .build());
        testDto.setTimeStamp(LocalDateTime.of(2024, 1, 15, 0, 0, 0));

        String s2sToken = "Bearer test-s2s-token";
        when(idamService.getServiceUserToken()).thenReturn("Bearer " + userToken);
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
            eq("Bearer test-s2s-token"),
            eq(CCD_CASE_ID),
            eq(Event.SET_HOME_OFFICE_STATUTORY_TIMEFRAME_STATUS.toString())
        )).thenReturn(mockStartEventDetails);

        SubmitEventDetails mockSubmitEventDetails = mock(SubmitEventDetails.class);
        when(mockSubmitEventDetails.getCallbackResponseStatusCode()).thenReturn(201);
        when(mockSubmitEventDetails.getCallbackResponseStatus()).thenReturn("Created");

        when(ccdDataApi.submitEventByCase(
            eq("Bearer test-user-token"),
            eq("Bearer test-s2s-token"),
            eq(CCD_CASE_ID),
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

        when(ccdDataApi.submitEventByCase(any(), any(), eq(CASE_ID), any()))
            .thenReturn(response);

        SubmitEventDetails result =
            ccdDataService.setHomeOfficeStatutoryTimeframeStatus(dto);

        assertNotNull(result);
        assertEquals(200, result.getCallbackResponseStatusCode());

        verify(asylumCase).read(STATUTORY_TIMEFRAME_24_WEEKS);
        verify(ccdDataApi).submitEventByCase(any(), any(), eq(CASE_ID), any());
    }

    @Test
    void shouldSetStatusNoWhenNoCohortsIncluded() {

        dto.setStf24weekCohorts(List.of(
            HomeOfficeStatutoryTimeframeDto.Stf24WeekCohort.builder()
                .name("HU")
                .included("false")
                .build()
        ));

        when(idamService.getServiceUserToken()).thenReturn("user");
        when(serviceAuthorization.generate()).thenReturn("s2s");
        when(dbUtils.getCaseId(HMCTS_REF_NUM)).thenReturn(CASE_ID);

        AsylumCase asylumCase = mock(AsylumCase.class);
        when(asylumCase.read(STATUTORY_TIMEFRAME_24_WEEKS))
            .thenReturn(Optional.empty());

        StartEventDetails start =
            buildStartEvent(asylumCase, Event.REMOVE_STATUTORY_TIMEFRAME_24_WEEKS);

        when(ccdDataApi.startEventByCase(any(), any(), eq(CASE_ID), any()))
            .thenReturn(start);

        SubmitEventDetails response = buildSubmitResponse();

        when(ccdDataApi.submitEventByCase(any(), any(), eq(CASE_ID), any()))
            .thenReturn(response);

        SubmitEventDetails result =
            ccdDataService.setHomeOfficeStatutoryTimeframeStatus(dto);

        assertNotNull(result);

        verify(ccdDataApi).submitEventByCase(any(), any(), eq(CASE_ID), any());
    }

    @Test
    void shouldThrowWhenStatusAlreadyExists() {

        when(idamService.getServiceUserToken()).thenReturn("user");
        when(serviceAuthorization.generate()).thenReturn("s2s");
        when(dbUtils.getCaseId(HMCTS_REF_NUM)).thenReturn(CASE_ID);

        StatutoryTimeframe24Weeks existing = mock(StatutoryTimeframe24Weeks.class);

    @Test
    void shouldThrowExceptionWhenStatutoryTimeframeStatusAlreadySet() {
        // Given
        String userToken = "test-user-token";
        String s2sToken = "Bearer test-s2s-token";
        testDto.setStf24weeks(HomeOfficeStatutoryTimeframeDto.Stf24Weeks.builder()
            .status("Yes")
            .cohorts(new String[]{"HU"})
            .build());

        when(idamService.getServiceUserToken()).thenReturn("Bearer " + userToken);
        when(serviceAuthorization.generate()).thenReturn(s2sToken);

        AsylumCase asylumCase = mock(AsylumCase.class);

        when(asylumCase.read(STATUTORY_TIMEFRAME_24_WEEKS))
            .thenReturn(Optional.of(existing));

        StartEventDetails start =
            buildStartEvent(asylumCase, Event.SET_HOME_OFFICE_STATUTORY_TIMEFRAME_STATUS);

        when(ccdDataApi.startEventByCase(any(), any(), eq(CASE_ID), any()))
            .thenReturn(start);

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
            eq("Bearer test-s2s-token"),
            eq(CCD_CASE_ID),
            eq(Event.SET_HOME_OFFICE_STATUTORY_TIMEFRAME_STATUS.toString())
        )).thenReturn(mockStartEventDetails);

        // When & Then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> ccdDataService.setHomeOfficeStatutoryTimeframeStatus(dto)
        );
    }

    @Test
    void nextHistoryIdShouldReturn1WhenEmpty() {

        assertEquals(
            "1",
            ccdDataService.nextHistoryId(Optional.empty())
        );
    }

    @Test
    void nextHistoryIdShouldIncrement() {

        StatutoryTimeframe24Weeks data = mock(StatutoryTimeframe24Weeks.class);

        List<IdValue<StatutoryTimeframe24WeeksHistory>> history = List.of(
            new IdValue<>("1", mock(StatutoryTimeframe24WeeksHistory.class)),
            new IdValue<>("3", mock(StatutoryTimeframe24WeeksHistory.class))
        );

        when(data.getHistory()).thenReturn(history);

        String result =
            ccdDataService.nextHistoryId(Optional.of(data));

        assertEquals("4", result);
    }

    @Test
    void shouldThrowExceptionWhenStatusAlreadySetToYes() {
        // Given
        String userToken = "test-user-token";
        testDto.setCcdCaseId("99999");
        testDto.setStf24weeks(HomeOfficeStatutoryTimeframeDto.Stf24Weeks.builder()
            .status("No")
            .cohorts(new String[]{"HU"})
            .build());

        String s2sToken = "Bearer test-s2s-token";
        when(idamService.getServiceUserToken()).thenReturn("Bearer " + userToken);
        when(serviceAuthorization.generate()).thenReturn(s2sToken);

        // Create existing data with multiple history entries
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
            eq("Bearer test-user-token"),
            eq("Bearer test-s2s-token"),
            eq("99999"),
            eq(Event.REMOVE_STATUTORY_TIMEFRAME_24_WEEKS.toString())
        )).thenReturn(mockStartEventDetails);

        // When & Then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> ccdDataService.setHomeOfficeStatutoryTimeframeStatus(testDto)
        );

        assertNotNull(result);
        assertEquals(1, result.getHistory().size());

        StatutoryTimeframe24WeeksHistory history =
            result.getHistory().get(0).getValue();

        assertEquals(YesOrNo.YES, history.getStatus());
        assertEquals("Home Office initial determination", history.getReason());
        assertEquals("Home Office Integration API", history.getUser());
        assertEquals(
            "2024-01-01T12:00:00Z",
            history.getDateTimeAdded()
        );
    }

    @Test
    void shouldGenerateS2SToken() {

        when(serviceAuthorization.generate()).thenReturn("token");

        String result = ccdDataService.generateS2SToken();

        assertEquals("token", result);
        verify(serviceAuthorization).generate();
    }

    @Test
    void shouldGetServiceUserToken() {

        when(idamService.getServiceUserToken()).thenReturn("abc");

        String result = ccdDataService.getServiceUserToken();

        assertEquals("abc", result);
        verify(idamService).getServiceUserToken();
    }

    @Test
    void shouldThrowCaseNotFoundExceptionWhenCaseIdIsNotValid() {
        // Given
        String userToken = "test-user-token";
        String s2sToken = "Bearer test-s2s-token";

        when(idamService.getServiceUserToken()).thenReturn("Bearer " + userToken);
        when(serviceAuthorization.generate()).thenReturn(s2sToken);

        when(ccdDataApi.startEventByCase(
            eq("Bearer test-user-token"),
            eq("Bearer test-s2s-token"),
            eq("12345"),
            eq(Event.SET_HOME_OFFICE_STATUTORY_TIMEFRAME_STATUS.toString())
        )).thenThrow(new HomeOfficeResponseException("StatusCode: 400, methodKey: CcdDataApi#startEventByCase, reason: null, message: Case ID is not valid"));

        // When & Then
        CaseNotFoundException exception = assertThrows(
            CaseNotFoundException.class,
            () -> ccdDataService.setHomeOfficeStatutoryTimeframeStatus(testDto)
        );

        when(idamService.getServiceUserToken()).thenReturn("user");
        when(serviceAuthorization.generate()).thenReturn("s2s");
        when(dbUtils.getCaseId(HMCTS_REF_NUM)).thenReturn(CASE_ID);

        when(ccdDataApi.startEventByCase(any(), any(), eq(CASE_ID), any()))
            .thenThrow(new RuntimeException("Case ID is not valid"));

        assertThrows(
            CaseNotFoundException.class,
            () -> ccdDataService.setHomeOfficeStatutoryTimeframeStatus(dto)
        );
    }

    @Test
    void shouldRethrowOtherExceptionsFromStartEventByCase() {
        // Given
        String userToken = "test-user-token";
        String s2sToken = "Bearer test-s2s-token";

        when(idamService.getServiceUserToken()).thenReturn("Bearer " + userToken);
        when(serviceAuthorization.generate()).thenReturn(s2sToken);

        RuntimeException originalException = new RuntimeException("Some other error");
        when(ccdDataApi.startEventByCase(
            eq("Bearer test-user-token"),
            eq("Bearer test-s2s-token"),
            eq("12345"),
            eq(Event.SET_HOME_OFFICE_STATUTORY_TIMEFRAME_STATUS.toString())
        )).thenThrow(originalException);

        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> ccdDataService.setHomeOfficeStatutoryTimeframeStatus(dto)
        );

        assertEquals("boom", thrown.getMessage());
    }

    @Test
    void shouldNormaliseUserTokenWhenBearerPrefixIsMissing() {
        when(idamService.getServiceUserToken()).thenReturn("test-user-token");
        when(serviceAuthorization.generate()).thenReturn("Bearer test-s2s-token");

        stubHappyPath("Bearer test-user-token", "Bearer test-s2s-token");

        ccdDataService.setHomeOfficeStatutoryTimeframeStatus(testDto);

        verify(ccdDataApi).startEventByCase(
            eq("Bearer test-user-token"),
            eq("Bearer test-s2s-token"),
            eq(CCD_CASE_ID),
            eq(Event.SET_HOME_OFFICE_STATUTORY_TIMEFRAME_STATUS.toString())
        );
    }

    @Test
    void shouldNormaliseUserTokenWhenBearerPrefixIsDuplicated() {
        when(idamService.getServiceUserToken()).thenReturn("Bearer Bearer test-user-token");
        when(serviceAuthorization.generate()).thenReturn("Bearer test-s2s-token");

        stubHappyPath("Bearer test-user-token", "Bearer test-s2s-token");

        ccdDataService.setHomeOfficeStatutoryTimeframeStatus(testDto);

        verify(ccdDataApi).startEventByCase(
            eq("Bearer test-user-token"),
            eq("Bearer test-s2s-token"),
            eq(CCD_CASE_ID),
            eq(Event.SET_HOME_OFFICE_STATUTORY_TIMEFRAME_STATUS.toString())
        );
    }

    @Test
    void shouldNormaliseS2STokenWhenBearerPrefixIsMissing() {
        when(idamService.getServiceUserToken()).thenReturn("Bearer test-user-token");
        when(serviceAuthorization.generate()).thenReturn("test-s2s-token");

        stubHappyPath("Bearer test-user-token", "Bearer test-s2s-token");

        ccdDataService.setHomeOfficeStatutoryTimeframeStatus(testDto);

        verify(ccdDataApi).startEventByCase(
            eq("Bearer test-user-token"),
            eq("Bearer test-s2s-token"),
            eq(CCD_CASE_ID),
            eq(Event.SET_HOME_OFFICE_STATUTORY_TIMEFRAME_STATUS.toString())
        );
    }

    @Test
    void shouldNormaliseS2STokenWhenBearerPrefixIsDuplicated() {
        when(idamService.getServiceUserToken()).thenReturn("Bearer test-user-token");
        when(serviceAuthorization.generate()).thenReturn("Bearer Bearer test-s2s-token");

        stubHappyPath("Bearer test-user-token", "Bearer test-s2s-token");

        ccdDataService.setHomeOfficeStatutoryTimeframeStatus(testDto);

        verify(ccdDataApi).startEventByCase(
            eq("Bearer test-user-token"),
            eq("Bearer test-s2s-token"),
            eq(CCD_CASE_ID),
            eq(Event.SET_HOME_OFFICE_STATUTORY_TIMEFRAME_STATUS.toString())
        );
    }

    @Test
    void shouldThrowWhenUserTokenIsNull() {
        when(idamService.getServiceUserToken()).thenReturn(null);

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> ccdDataService.setHomeOfficeStatutoryTimeframeStatus(testDto)
        );

        assertEquals("Token is null or blank", exception.getMessage());
        verifyNoInteractions(ccdDataApi);
    }

    @Test
    void shouldThrowWhenUserTokenIsBlank() {
        when(idamService.getServiceUserToken()).thenReturn("   ");

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> ccdDataService.setHomeOfficeStatutoryTimeframeStatus(testDto)
        );

        assertEquals("Token is null or blank", exception.getMessage());
        verifyNoInteractions(ccdDataApi);
    }

    @Test
    void shouldReturnServiceUserToken() {
        String expectedToken = "Bearer test-user-token";
        when(idamService.getServiceUserToken()).thenReturn(expectedToken);

        String actualToken = ccdDataService.getServiceUserToken();

        assertEquals(expectedToken, actualToken);
        verify(idamService).getServiceUserToken();
    }

    private void stubHappyPath(String userToken, String s2sToken) {
        StartEventDetails mockStartEventDetails = mock(StartEventDetails.class);
        @SuppressWarnings("unchecked")
        CaseDetails<AsylumCase> mockCaseDetails = mock(CaseDetails.class);
        when(mockCaseDetails.getCaseData()).thenReturn(mock(AsylumCase.class));
        when(mockStartEventDetails.getCaseDetails()).thenReturn(mockCaseDetails);
        when(mockStartEventDetails.getToken()).thenReturn("test-event-token");
        when(mockStartEventDetails.getEventId()).thenReturn(Event.SET_HOME_OFFICE_STATUTORY_TIMEFRAME_STATUS);

        when(ccdDataApi.startEventByCase(
            eq(userToken), eq(s2sToken), eq(CCD_CASE_ID),
            eq(Event.SET_HOME_OFFICE_STATUTORY_TIMEFRAME_STATUS.toString())
        )).thenReturn(mockStartEventDetails);

        SubmitEventDetails mockSubmitEventDetails = mock(SubmitEventDetails.class);
        when(mockSubmitEventDetails.getCallbackResponseStatusCode()).thenReturn(201);
        when(ccdDataApi.submitEventByCase(
            eq(userToken), eq(s2sToken), eq(CCD_CASE_ID), any(CaseDataContent.class)
        )).thenReturn(mockSubmitEventDetails);
    }

}
