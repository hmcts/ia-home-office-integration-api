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

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
}
