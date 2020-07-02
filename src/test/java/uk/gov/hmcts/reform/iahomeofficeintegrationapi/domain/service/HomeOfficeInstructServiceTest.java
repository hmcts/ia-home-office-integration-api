package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.config.HomeOfficeProperties.LookupReferenceData;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.CodeWithDescription;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.CourtType;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeInstruct;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeInstructResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.MessageHeader;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.Outcome;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.HomeOfficeInstructApi;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.config.HomeOfficeProperties;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class HomeOfficeInstructServiceTest {

    private final String someHoReference = "some-ho-reference";
    private final String someCaseReference = "some-case";
    private final String someCorrelationId = "some-id";

    @Mock
    private HomeOfficeProperties homeOfficeProperties;
    @Mock
    private HomeOfficeInstructApi homeOfficeInstructApi;
    @Mock
    private HomeOfficeInstruct homeOfficeInstruct;

    private HomeOfficeInstructService homeOfficeInstructService;

    @BeforeEach
    public void setUp() {

        homeOfficeInstructService = new HomeOfficeInstructService(
            homeOfficeProperties, homeOfficeInstructApi);
    }

    @Test
    public void should_return_message_header_for_valid_request_inputs() {

        when(homeOfficeProperties.getCodes()).thenReturn(getHomeOfficeReferenceData());
        when(homeOfficeInstructApi.sendNotification(any()))
            .thenReturn(getResponse());

        HomeOfficeInstructResponse instructResponse = homeOfficeInstructService.sendNotification(
            someHoReference, someCaseReference, someCorrelationId);

        assertNotNull(instructResponse.getMessageHeader());
        assertThat(instructResponse.getMessageHeader().getCorrelationId()).isNotBlank();
        assertThat(instructResponse.getMessageHeader().getEventDateTime()).isNotBlank();
        assertEquals(someCorrelationId, instructResponse.getMessageHeader().getCorrelationId());
        assertEquals("some-time", instructResponse.getMessageHeader().getEventDateTime());

    }

    @Test
    public void returns_values_in_request_body() {

        doReturn(getHomeOfficeReferenceData()).when(homeOfficeProperties).getCodes();

        HomeOfficeInstruct request = homeOfficeInstructService.makeRequestBody(
            someHoReference, someCaseReference, someCorrelationId);

        assertNotNull(request);
        assertEquals(someHoReference, request.getHoReference());
        assertEquals("HMCTS_CHALLENGE_REF", request.getConsumerReference().getCode());
        assertEquals("HMCTS challenge reference",
            request.getConsumerReference().getDescription());
        assertEquals("HMCTS",
            request.getConsumerReference().getConsumer().getCode());
        assertEquals("HM Courts and Tribunal Service",
            request.getConsumerReference().getConsumer().getDescription());
        assertEquals(someCaseReference, request.getConsumerReference().getValue());
        assertEquals(CourtType.FIRST_TIER.toString(), request.getCourtOutcome().getCourtType());
        assertEquals(Outcome.ALLOWED.toString(), request.getCourtOutcome().getOutcome());
        assertEquals("HMCTS", request.getMessageHeader().getConsumer().getCode());
        assertEquals("HM Courts and Tribunal Service",
            request.getMessageHeader().getConsumer().getDescription());
        assertNotNull(request.getMessageHeader().getEventDateTime());
        assertEquals(someCorrelationId, request.getMessageHeader().getCorrelationId());

    }

    @Test
    public void should_throw_for_null_case() {

        assertThatThrownBy(() -> homeOfficeInstructService.sendNotification(
            null, null, someCorrelationId))
            .isExactlyInstanceOf(NullPointerException.class);
    }

    private Map<String, LookupReferenceData> getHomeOfficeReferenceData() {

        final Map<String, LookupReferenceData> consumerMap = new HashMap<>();

        LookupReferenceData lookupReferenceData = new LookupReferenceData();
        lookupReferenceData.setCode("HMCTS_CHALLENGE_REF");
        lookupReferenceData.setDescription("HMCTS challenge reference");
        consumerMap.put("consumerReference", lookupReferenceData);

        lookupReferenceData = new LookupReferenceData();
        lookupReferenceData.setCode("HMCTS");
        lookupReferenceData.setDescription("HM Courts and Tribunal Service");
        consumerMap.put("consumer", lookupReferenceData);

        return consumerMap;
    }

    private HomeOfficeInstructResponse getResponse() {
        return new HomeOfficeInstructResponse(
            new MessageHeader(
                new CodeWithDescription("HMCTS", "HM Courts and Tribunal Service"),
                someCorrelationId,
                "some-time"),
            null);
    }
}
