package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeStatutoryTimeframeDto;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.StatutoryTimeFrame24WeeksFieldValue;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.IdamService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.model.idam.UserInfo;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CcdDataServiceTest {

    @Mock
    private CoreCaseDataApi coreCaseDataApi;
    @Mock
    private IdamService systemTokenGenerator;
    @Mock
    private AuthTokenGenerator serviceAuthorization;

    private CcdDataService ccdDataService;

    @BeforeEach
    void setUp() {
        ccdDataService = new CcdDataService(
            coreCaseDataApi,
            systemTokenGenerator,
            serviceAuthorization
        );
    }

    @Test
    void toStf4w_shouldConvertDtoToCorrectFormat() {
        // Given
        String testId = "test-id-123";
        LocalDate testDate = LocalDate.of(2023, 12, 25);
        HomeOfficeStatutoryTimeframeDto dto = HomeOfficeStatutoryTimeframeDto.builder()
            .ccdCaseNumber("1234567890")
            .hoStatutoryTimeframeStatus(true)
            .timeStamp(testDate)
            .build();

        // When
        List<IdValue<StatutoryTimeFrame24WeeksFieldValue>> result = ccdDataService.toStf4w(testId, dto);

        // Then
        assertThat(result).hasSize(1);
        
        IdValue<StatutoryTimeFrame24WeeksFieldValue> idValue = result.get(0);
        assertThat(idValue.getId()).isEqualTo(testId);
        
        StatutoryTimeFrame24WeeksFieldValue value = idValue.getValue();
        assertThat(value.getStatus()).isEqualTo(YesOrNo.YES);
        assertThat(value.getReason()).isEqualTo("Home Office statutory timeframe update");
        assertThat(value.getUser()).isEqualTo("Home Office Integration API");
        assertThat(value.getDateTimeAdded()).isEqualTo("2023-12-25T00:00:00Z");
    }

    @Test
    void setHomeOfficeStatutoryTimeframeStatus_shouldUpdateCaseSuccessfully() {
        // Given
        String caseId = "1234567890";
        String userToken = "user-token";
        String s2sToken = "s2s-token";
        String userId = "user-id";
        String eventToken = "event-token";
        
        HomeOfficeStatutoryTimeframeDto dto = HomeOfficeStatutoryTimeframeDto.builder()
            .ccdCaseNumber(caseId)
            .hoStatutoryTimeframeStatus(true)
            .timeStamp(LocalDate.of(2023, 12, 25))
            .build();
        
        StartEventResponse startEventResponse = StartEventResponse.builder()
            .token(eventToken)
            .build();
        
        CaseDetails expectedCaseDetails = CaseDetails.builder()
            .id(Long.parseLong(caseId))
            .callbackResponseStatus("SUCCESS")
            .build();
        
        UserInfo userInfo = new UserInfo("email@test.com", userId, null, "Test User", "Test", "User");

        // When
        when(systemTokenGenerator.getServiceUserToken()).thenReturn(userToken);
        when(serviceAuthorization.generate()).thenReturn(s2sToken);
        when(systemTokenGenerator.getUserInfo("Bearer " + userToken)).thenReturn(userInfo);
        when(coreCaseDataApi.startEvent(anyString(), anyString(), eq(caseId), eq(userId)))
            .thenReturn(startEventResponse);
        when(coreCaseDataApi.submitEventForCaseWorker(anyString(), anyString(), anyString(), 
            anyString(), anyString(), eq(caseId), anyBoolean(), any()))
            .thenReturn(expectedCaseDetails);

        CaseDetails result = ccdDataService.setHomeOfficeStatutoryTimeframeStatus(dto);

        // Then
        assertThat(result).isEqualTo(expectedCaseDetails);
        verify(systemTokenGenerator).getServiceUserToken();
        verify(serviceAuthorization).generate();
        verify(systemTokenGenerator).getUserInfo("Bearer " + userToken);
        verify(coreCaseDataApi).startEvent("Bearer " + userToken, s2sToken, caseId, userId);
        verify(coreCaseDataApi).submitEventForCaseWorker(eq("Bearer " + userToken), eq(s2sToken), 
            eq(userId), eq("IA"), eq("Asylum"), eq(caseId), eq(true), any());
    }
}
