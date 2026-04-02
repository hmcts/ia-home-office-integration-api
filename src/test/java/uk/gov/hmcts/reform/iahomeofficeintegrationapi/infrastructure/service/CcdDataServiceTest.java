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
    void shouldThrowRuntimeWhenS2STokenFails() {

        when(idamService.getServiceUserToken()).thenReturn("token");
        when(serviceAuthorization.generate()).thenThrow(new RuntimeException("boom"));

        RuntimeException ex = assertThrows(
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

        IllegalStateException ex = assertThrows(
            IllegalStateException.class,
            () -> ccdDataService.setHomeOfficeStatutoryTimeframeStatus(dto)
        );

        assertTrue(ex.getMessage().contains("Case details is null"));
    }

    @Test
    void shouldSuccessfullySetStatusYes() {

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

        when(ccdDataApi.startEventByCase(any(), any(), eq(CASE_ID), any()))
            .thenReturn(start);

        SubmitEventDetails response = buildSubmitResponse();

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

        List<IdValue<StatutoryTimeframe24WeeksHistory>> history =
            List.of(new IdValue<>("1", mock(StatutoryTimeframe24WeeksHistory.class)));

        when(existing.getHistory()).thenReturn(history);

        AsylumCase asylumCase = mock(AsylumCase.class);

        when(asylumCase.read(STATUTORY_TIMEFRAME_24_WEEKS))
            .thenReturn(Optional.of(existing));

        StartEventDetails start =
            buildStartEvent(asylumCase, Event.SET_HOME_OFFICE_STATUTORY_TIMEFRAME_STATUS);

        when(ccdDataApi.startEventByCase(any(), any(), eq(CASE_ID), any()))
            .thenReturn(start);

        assertThrows(
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
    void toStf24wShouldCreateCorrectHistoryEntry() {

        StatutoryTimeframe24Weeks result =
            ccdDataService.toStf24w("1", YesOrNo.YES, dto);

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
    void shouldThrowCaseNotFoundWhenApiReturnsInvalidCaseId() {

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
    void shouldRethrowUnexpectedException() {

        when(idamService.getServiceUserToken()).thenReturn("user");
        when(serviceAuthorization.generate()).thenReturn("s2s");
        when(dbUtils.getCaseId(HMCTS_REF_NUM)).thenReturn(CASE_ID);

        RuntimeException ex = new RuntimeException("boom");

        when(ccdDataApi.startEventByCase(any(), any(), eq(CASE_ID), any()))
            .thenThrow(ex);

        RuntimeException thrown = assertThrows(
            RuntimeException.class,
            () -> ccdDataService.setHomeOfficeStatutoryTimeframeStatus(dto)
        );

        assertEquals("boom", thrown.getMessage());
    }

}