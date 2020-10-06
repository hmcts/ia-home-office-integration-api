package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Qualifier;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.CodeWithDescription;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeInstruct;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeInstructResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.MessageHeader;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.RequestEvidenceBundleInstructMessage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.HomeOfficeInstructApi;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security.AccessTokenProvider;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class HomeOfficeInstructServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private HomeOfficeInstructApi homeOfficeInstructApi;
    @Mock
    private @Qualifier("requestUser")
    AccessTokenProvider accessTokenProvider;

    private HomeOfficeInstructService homeOfficeInstructService;

    @BeforeEach
    public void setUp() {

        homeOfficeInstructService = new HomeOfficeInstructService(
            homeOfficeInstructApi, accessTokenProvider, objectMapper);
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

    private void assertMessageHeader(MessageHeader messageHeader) {

        assertThat(messageHeader).isNotNull();
        assertThat(messageHeader.getCorrelationId()).isEqualTo(someCorrelationId);
        assertThat(messageHeader.getConsumer().getCode()).isEqualTo("HMCTS");
        assertThat(messageHeader.getConsumer().getDescription()).isEqualTo("HM Courts and Tribunal Service");
    }

    private void assertConsumerReference(ConsumerReference consumerReference) {

        assertThat(consumerReference).isNotNull();
        assertThat(consumerReference.getValue()).isEqualTo(someCaseReference);
        assertThat(consumerReference.getCode()).isEqualTo("HMCTS_CHALLENGE_REF");
        assertThat(consumerReference.getConsumer().getCode()).isEqualTo("HMCTS");
        assertThat(consumerReference.getConsumer().getDescription())
            .isEqualTo("HM Courts and Tribunal Service");
    }

    private void assertPerson(Person person) {

        assertThat(person).isNotNull();
        assertThat(person.getGivenName()).isEqualTo(firstName);
        assertThat(person.getFamilyName()).isEqualTo(surname);
        assertThat(person.getFullName()).isEqualTo(firstName + " " + surname);
        assertThat(person.getDayOfBirth()).isEqualTo(1);
        assertThat(person.getMonthOfBirth()).isEqualTo(1);
        assertThat(person.getYearOfBirth()).isEqualTo(2000);
        assertThat(person.getGender()).isNull();
    }

    private void assertNationality(Person person) {
        assertThat(person.getNationality()).isNotNull();
        assertThat(person.getNationality().getCode()).isEqualTo("AU");
        assertThat(person.getNationality().getDescription()).isEqualTo("Australia");
    }

    private void setupApplicantDetails() {

        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(firstName));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(surname));
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of("2000-01-01"));
    }

    private HomeOfficeInstruct buildRequestMessage() {

        String someHoReference = "some-ho-reference";
        return
            new RequestEvidenceBundleInstructMessage(
                null, someHoReference, null, "someMessageType", "01-01-2021", null, "direction content"
            );
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
