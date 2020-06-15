package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.config.HomeOfficeProperties.LookupReferenceData;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ConsumerInstruct;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ConsumerReference;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ConsumerType;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.CourtOutcome;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.CourtType;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeInstruct;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.MessageHeader;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.MessageType;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.Outcome;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.HomeOfficeInstructApi;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.util.HomeOfficeApiUtil;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.config.HomeOfficeProperties;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class HomeOfficeInstructServiceTest {

    private final String someHoReference = "some-ho-reference";
    private final String someCaseReference = "some-case";

    @Mock private HomeOfficeProperties homeOfficeProperties;
    @Mock private HomeOfficeInstructApi homeOfficeInstructApi;
    @Mock private HomeOfficeInstruct homeOfficeInstruct;

    private HomeOfficeInstructService homeOfficeInstructService;

    @BeforeEach
    public void setUp() {

        homeOfficeInstructService = new HomeOfficeInstructService(homeOfficeProperties, homeOfficeInstructApi);
    }

    @Test
    public void should_return_message_header_for_valid_request_inputs() {

        when(homeOfficeProperties.getHomeOfficeReferenceData()).thenReturn(getHomeOfficeReferenceData());
        when(homeOfficeInstructApi.sendNotification(any())).thenReturn(getMessageHeader());

        MessageHeader messageHeader = homeOfficeInstructService.sendNotification(
            someHoReference,someCaseReference);

        assertNotNull(messageHeader);
        assertThat(messageHeader.getCorrelationId()).isNotBlank();
        assertThat(messageHeader.getEventDateTime()).isNotBlank();
        assertEquals("AAAAAAAAA", messageHeader.getCorrelationId());
        assertEquals("2020-06-15T17:32:28Z", messageHeader.getEventDateTime());

    }

    @Test
    public void should_return_message_header_for_valid_request_body() {

        when(homeOfficeInstructApi.sendNotification(any())).thenReturn(getMessageHeader());

        MessageHeader messageHeader = homeOfficeInstructService.makeRequest(makeSampleRequestBody());

        assertNotNull(messageHeader);
        assertThat(messageHeader.getCorrelationId()).isNotBlank();
        assertThat(messageHeader.getEventDateTime()).isNotBlank();
        assertEquals("AAAAAAAAA", messageHeader.getCorrelationId());
        assertEquals("2020-06-15T17:32:28Z", messageHeader.getEventDateTime());

    }

    @Test
    public void returns_values_in_request_body() {

        when(homeOfficeProperties.getHomeOfficeReferenceData()).thenReturn(getHomeOfficeReferenceData());

        HomeOfficeInstruct request = homeOfficeInstructService.makeRequestBody(someHoReference, someCaseReference);

        assertNotNull(request);
        assertEquals(someHoReference, request.getHoReference());
        assertEquals("HMCTS_CHALLENGE_REF", request.getConsumerReference().getConsumerInstruct().getCode());
        assertEquals("HMCTS challenge reference",
            request.getConsumerReference().getConsumerInstruct().getDescription());
        assertEquals("HMCTS",
            request.getConsumerReference().getConsumerInstruct().getConsumerType().getCode());
        assertEquals("HM Courts and Tribunal Service",
            request.getConsumerReference().getConsumerInstruct().getConsumerType().getDescription());
        assertEquals(someCaseReference, request.getConsumerReference().getConsumerInstruct().getValue());
        assertEquals(CourtType.FIRST_TIER.toString(), request.getCourtOutcome().getCourtType());
        assertEquals(Outcome.ALLOWED.toString(), request.getCourtOutcome().getOutcome());
        assertEquals("HMCTS", request.getMessageHeader().getConsumerType().getCode());
        assertEquals("HM Courts and Tribunal Service",
            request.getMessageHeader().getConsumerType().getDescription());
        assertNotNull(request.getMessageHeader().getEventDateTime());
        assertNotNull(request.getMessageHeader().getCorrelationId());

    }

    @Test
    public void should_throw_for_null_case() {

        assertThatThrownBy(() -> homeOfficeInstructService.sendNotification(null, null))
            .isExactlyInstanceOf(NullPointerException.class);
    }

    private Map<String, LookupReferenceData> getHomeOfficeReferenceData() {

        final Map<String, LookupReferenceData> consumerMap = new HashMap<>();

        LookupReferenceData lookupReferenceData = new LookupReferenceData();
        lookupReferenceData.setCode("HMCTS");
        lookupReferenceData.setDescription("HM Courts and Tribunal Service");
        consumerMap.put("consumer", lookupReferenceData);

        lookupReferenceData = new LookupReferenceData();
        lookupReferenceData.setCode("HMCTS_CHALLENGE_REF");
        lookupReferenceData.setDescription("HMCTS challenge reference");
        consumerMap.put("consumerInstruct", lookupReferenceData);

        return consumerMap;
    }

    private MessageHeader getMessageHeader() {
        return new MessageHeader(
            new ConsumerType("HMCTS", "HM Courts and Tribunal Service"),
            "AAAAAAAAA",
            "2020-06-15T17:32:28Z");
    }

    private HomeOfficeInstruct makeSampleRequestBody() {

        final ConsumerType consumerType = new ConsumerType("HMCTS", "HM Courts and Tribunal Service");

        ConsumerInstruct consumerInstruct = new ConsumerInstruct(
            "HMCTS_CHALLENGE_REF",
            consumerType,
            "HMCTS challenge reference",
            someCaseReference
        );

        CourtOutcome courtOutcome = new CourtOutcome(
            CourtType.FIRST_TIER.toString(),
            Outcome.ALLOWED.toString());
        MessageHeader messageHeader = new MessageHeader(
            consumerType,
            HomeOfficeApiUtil.generateUuid(),
            HomeOfficeApiUtil.getCurrentDateTime());

        return new HomeOfficeInstruct(
            new ConsumerReference(consumerInstruct),
            courtOutcome,
            someHoReference,
            messageHeader,
            MessageType.REQUEST_CHALLENGE_END.toString()
        );

    }
}
