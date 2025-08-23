package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Qualifier;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.CodeWithDescription;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ConsumerReference;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeInstruct;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeInstructResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.MessageHeader;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.RequestEvidenceBundleInstructMessage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.HomeOfficeInstructApi;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security.AccessTokenProvider;


@ExtendWith(MockitoExtension.class)
class HomeOfficeInstructServiceTest {

    @Mock
    private HomeOfficeInstructApi homeOfficeInstructApi;
    @Mock
    private @Qualifier("requestUser")
    AccessTokenProvider accessTokenProvider;

    private HomeOfficeInstructService homeOfficeInstructService;

    @BeforeEach
    public void setUp() {
        homeOfficeInstructService = new HomeOfficeInstructService(
            homeOfficeInstructApi, accessTokenProvider);
    }

    @Test
    void shouldSetInstructStatusOkForValidRequest() {

        when(accessTokenProvider.getAccessToken()).thenReturn("some-access-token");
        when(homeOfficeInstructApi.sendNotification(anyString(), any(HomeOfficeInstruct.class)))
            .thenReturn(getResponse());

        final String response =
            homeOfficeInstructService.sendNotification(buildRequestMessage());

        assertThat(response).isEqualTo("OK");
    }

    @Test
    void shouldSetInstructStatusErrorForNullResponse() {

        when(accessTokenProvider.getAccessToken()).thenReturn("some-access-token");
        when(homeOfficeInstructApi.sendNotification(anyString(), any(HomeOfficeInstruct.class))).thenReturn(null);

        final String response =
            homeOfficeInstructService.sendNotification(buildRequestMessage());

        assertThat(response).isEqualTo("FAIL");
    }

    private HomeOfficeInstruct buildRequestMessage() {

        String someHoReference = "some-ho-reference";
        return
            new RequestEvidenceBundleInstructMessage(
                buildConsumerReference(), someHoReference, buildTestMessageHeader(),
                "someMessageType", "01-01-2021", null, "direction content"
            );
    }

    private MessageHeader buildTestMessageHeader() {
        return new MessageHeader(new CodeWithDescription("", ""), "test-correlation-id", "01-01-2021");
    }

    private ConsumerReference buildConsumerReference() {
        return new ConsumerReference("hmcts", new CodeWithDescription("", ""), "HMCTS API", "HMCTS REF");
    }

    private HomeOfficeInstructResponse getResponse() {

        String someCorrelationId = "some-id";
        return new HomeOfficeInstructResponse(
            new MessageHeader(
                new CodeWithDescription("HMCTS", "HM Courts and Tribunal Service"),
                someCorrelationId,
                "some-time"),
            null);
    }
}
