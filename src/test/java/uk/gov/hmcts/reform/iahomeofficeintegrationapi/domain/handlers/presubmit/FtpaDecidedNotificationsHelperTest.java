package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.presubmit;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.valueOf;

import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AppealDecidedInstructMessage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.CourtType;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.MessageType;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.Outcome;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.FtpaAppealDecidedNote;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.FtpaDecidedNotificationsHelper;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.HomeOfficeInstructService;


@ExtendWith(MockitoExtension.class)
class FtpaDecidedNotificationsHelperTest  extends AbstractNotificationsHandlerTestBase {

    @Mock
    private HomeOfficeInstructService homeOfficeInstructService;

    @Captor
    private ArgumentCaptor<AppealDecidedInstructMessage> appealDecidedInstructMessageCaptor;

    private FtpaDecidedNotificationsHelper ftpaDecidedNotificationsHelper;

    @BeforeEach
    void setUpHelper() {
        ftpaDecidedNotificationsHelper = new FtpaDecidedNotificationsHelper();
    }

    @ParameterizedTest
    @CsvSource(
        value = {
            "appellant:granted", "appellant:partiallyGranted", "appellant:refused", "appellant:notAdmitted",
            "respondent:granted", "respondent:partiallyGranted", "respondent:refused", "respondent:notAdmitted"
        },
        delimiter = ':'
    )
    @MockitoSettings(strictness = Strictness.WARN)
    void check_helper_returns_case_data_for_valid_input_leadership_judge(
        String applicantType, String ftpaDecisionOutcome
    ) {

        setupCase(Event.LEADERSHIP_JUDGE_FTPA_DECISION);
        setupCaseData();
        setupHelperResponses();
        setupFtpaDecisionData(applicantType, ftpaDecisionOutcome);

        when(homeOfficeInstructService.sendNotification(any(AppealDecidedInstructMessage.class)))
            .thenReturn("OK");

        ftpaDecidedNotificationsHelper.handleFtpaDecidedNotification(
            asylumCase, notificationsHelper, homeOfficeInstructService, null, "");

        verify(homeOfficeInstructService).sendNotification(appealDecidedInstructMessageCaptor.capture());

        final AppealDecidedInstructMessage instructMessage = appealDecidedInstructMessageCaptor.getValue();

        assertNotificationInstructMessage(instructMessage, applicantType, ftpaDecisionOutcome);

        verify(asylumCase, times(1)).write(
            valueOf(format("HOME_OFFICE_FTPA_%s_DECIDED_INSTRUCT_STATUS", applicantType.toUpperCase())), "OK");

    }

    @ParameterizedTest
    @CsvSource(
        value = {
            "appellant:granted", "appellant:partiallyGranted", "appellant:refused", "appellant:notAdmitted",
            "respondent:granted", "respondent:partiallyGranted", "respondent:refused", "respondent:notAdmitted"
        },
        delimiter = ':'
    )
    @MockitoSettings(strictness = Strictness.WARN)
    void check_helper_returns_error_status_leadership_judge(String applicantType, String ftpaDecisionOutcome) {

        setupCase(Event.LEADERSHIP_JUDGE_FTPA_DECISION);
        setupCaseData();
        setupHelperResponses();
        setupFtpaDecisionData(applicantType, ftpaDecisionOutcome);
        when(homeOfficeInstructService.sendNotification(any(AppealDecidedInstructMessage.class)))
            .thenReturn("FAIL");

        ftpaDecidedNotificationsHelper.handleFtpaDecidedNotification(
            asylumCase, notificationsHelper, homeOfficeInstructService, null, "");

        verify(homeOfficeInstructService).sendNotification(appealDecidedInstructMessageCaptor.capture());

        final AppealDecidedInstructMessage instructMessage = appealDecidedInstructMessageCaptor.getValue();
        assertNotificationInstructMessage(instructMessage, applicantType, ftpaDecisionOutcome);
        verify(asylumCase, times(1)).write(
            valueOf(format("HOME_OFFICE_FTPA_%s_DECIDED_INSTRUCT_STATUS", applicantType.toUpperCase())), "FAIL");

    }

    @ParameterizedTest
    @CsvSource(
        value = {
            "appellant:granted", "appellant:partiallyGranted", "appellant:refused", "appellant:reheardRule35",
            "appellant:reheardRule32", "respondent:granted", "respondent:partiallyGranted",
            "respondent:refused", "respondent:reheardRule35", "appellant:reheardRule32"
        },
        delimiter = ':'
    )
    @MockitoSettings(strictness = Strictness.WARN)
    void check_helper_returns_case_data_for_valid_input_resident_judge(
        String applicantType, String ftpaDecisionOutcome) {

        setupCase(Event.RESIDENT_JUDGE_FTPA_DECISION);
        setupCaseData();
        setupHelperResponses();
        setupFtpaRjDecisionData(applicantType, ftpaDecisionOutcome);

        when(homeOfficeInstructService.sendNotification(any(AppealDecidedInstructMessage.class)))
            .thenReturn("OK");

        ftpaDecidedNotificationsHelper.handleFtpaDecidedNotification(
            asylumCase, notificationsHelper, homeOfficeInstructService, null, "RJ_");

        verify(homeOfficeInstructService).sendNotification(appealDecidedInstructMessageCaptor.capture());

        final AppealDecidedInstructMessage instructMessage = appealDecidedInstructMessageCaptor.getValue();

        assertNotificationInstructMessageResidentJudge(instructMessage, applicantType, ftpaDecisionOutcome, null);

        verify(asylumCase, times(1)).write(
            valueOf(format("HOME_OFFICE_FTPA_%s_DECIDED_INSTRUCT_STATUS", applicantType.toUpperCase())), "OK");

    }

    @ParameterizedTest
    @CsvSource(
        value = {
            "appellant:remadeRule32:allowed", "appellant:remadeRule32:dismissed",
            "respondent:remadeRule32:allowed", "respondent:remadeRule32:dismissed"
        },
        delimiter = ':'
    )
    @MockitoSettings(strictness = Strictness.WARN)
    void check_helper_returns_case_data_for_valid_remade_input_resident_judge(
        String applicantType, String ftpaDecisionOutcome, String remadeDecision
    ) {

        setupCase(Event.RESIDENT_JUDGE_FTPA_DECISION);
        setupCaseData();
        setupHelperResponses();
        setupFtpaRjDecisionData(applicantType, ftpaDecisionOutcome);

        when(asylumCase.read(
            valueOf(format("FTPA_%s_DECISION_REMADE_RULE_32", applicantType.toUpperCase())), String.class)
        ).thenReturn(Optional.of(remadeDecision));

        when(homeOfficeInstructService.sendNotification(any(AppealDecidedInstructMessage.class)))
            .thenReturn("OK");

        ftpaDecidedNotificationsHelper.handleFtpaDecidedNotification(
            asylumCase, notificationsHelper, homeOfficeInstructService, null, "RJ_");

        verify(asylumCase).read(
            valueOf(format("FTPA_%s_DECISION_REMADE_RULE_32", applicantType.toUpperCase())), String.class);

        verify(homeOfficeInstructService).sendNotification(appealDecidedInstructMessageCaptor.capture());

        final AppealDecidedInstructMessage instructMessage = appealDecidedInstructMessageCaptor.getValue();

        assertNotificationInstructMessageResidentJudge(
            instructMessage, applicantType, ftpaDecisionOutcome, remadeDecision);

        verify(asylumCase, times(1)).write(
            valueOf(format("HOME_OFFICE_FTPA_%s_DECIDED_INSTRUCT_STATUS", applicantType.toUpperCase())), "OK");

    }

    @ParameterizedTest
    @CsvSource(
        value = {
            "appellant:granted", "appellant:partiallyGranted", "appellant:refused", "appellant:reheardRule35",
            "appellant:reheardRule32", "respondent:granted", "respondent:partiallyGranted",
            "respondent:refused", "respondent:reheardRule35", "appellant:reheardRule32"
        },
        delimiter = ':'
    )
    @MockitoSettings(strictness = Strictness.WARN)
    void check_helper_returns_error_status_resident_judge(String applicantType, String ftpaDecisionOutcome) {

        setupCase(Event.RESIDENT_JUDGE_FTPA_DECISION);
        setupCaseData();
        setupHelperResponses();
        setupFtpaRjDecisionData(applicantType, ftpaDecisionOutcome);
        when(homeOfficeInstructService.sendNotification(any(AppealDecidedInstructMessage.class)))
            .thenReturn("FAIL");

        ftpaDecidedNotificationsHelper.handleFtpaDecidedNotification(
            asylumCase, notificationsHelper, homeOfficeInstructService, null, "RJ_");

        verify(homeOfficeInstructService).sendNotification(appealDecidedInstructMessageCaptor.capture());

        final AppealDecidedInstructMessage instructMessage = appealDecidedInstructMessageCaptor.getValue();
        assertNotificationInstructMessageResidentJudge(instructMessage, applicantType, ftpaDecisionOutcome, null);

        verify(asylumCase, times(1)).write(
            valueOf(format("HOME_OFFICE_FTPA_%s_DECIDED_INSTRUCT_STATUS", applicantType.toUpperCase())), "FAIL");

    }

    @Test
    void should_respond_fail_for_case_reference_null_value() {

        String applicantType = "appellant";

        when(asylumCase.read(AsylumCaseDefinition.FTPA_APPLICANT_TYPE, String.class))
            .thenReturn(Optional.of(applicantType));

        final String notificationStatus = ftpaDecidedNotificationsHelper.handleFtpaDecidedNotification(
            asylumCase, notificationsHelper, homeOfficeInstructService, Event.LEADERSHIP_JUDGE_FTPA_DECISION, "");

        assertThat(notificationStatus).isEqualTo("FAIL");

        verify(asylumCase, times(1)).write(
            valueOf(format("HOME_OFFICE_FTPA_%s_DECIDED_INSTRUCT_STATUS", applicantType.toUpperCase())), "FAIL");

    }


    private void setupFtpaDecisionData(String applicantType, String ftpaDecisionOutcome) {

        when(asylumCase.read(AsylumCaseDefinition.FTPA_APPLICANT_TYPE, String.class))
            .thenReturn(Optional.of(applicantType));

        when(asylumCase.read(
            valueOf(format("FTPA_%s_DECISION_OUTCOME_TYPE", applicantType.toUpperCase())), String.class)
        ).thenReturn(Optional.of(ftpaDecisionOutcome));
    }

    private void assertNotificationInstructMessage(
        AppealDecidedInstructMessage instructMessage, String applicantType, String ftpaDecisionOutcome) {

        assertThat(instructMessage.getConsumerReference()).isEqualTo(consumerReference);
        assertThat(instructMessage.getMessageHeader()).isEqualTo(messageHeader);
        assertThat(instructMessage.getHoReference()).isEqualTo(someDocumentReference);
        assertThat(instructMessage.getNote())
            .contains(FtpaAppealDecidedNote.fromId(ftpaDecisionOutcome + "_" + applicantType).getValue());

        assertThat(instructMessage.getMessageType()).isEqualTo(MessageType.COURT_OUTCOME.name());
        assertThat(instructMessage.getCourtOutcome().getCourtType()).isEqualTo(CourtType.FIRST_TIER);
        assertThat(instructMessage.getCourtOutcome().getOutcome()).isIn(Arrays.asList(Outcome.values()));
    }


    private void setupFtpaRjDecisionData(String applicantType, String ftpaDecisionOutcome) {

        when(asylumCase.read(AsylumCaseDefinition.FTPA_APPLICANT_TYPE, String.class))
            .thenReturn(Optional.of(applicantType));

        when(asylumCase.read(
            valueOf(format("FTPA_%s_RJ_DECISION_OUTCOME_TYPE", applicantType.toUpperCase())), String.class)
        ).thenReturn(Optional.of(ftpaDecisionOutcome));
    }

    private void assertNotificationInstructMessageResidentJudge(
        AppealDecidedInstructMessage instructMessage, String applicantType,
        String ftpaDecisionOutcome, String remadeDecision
    ) {

        assertThat(instructMessage.getConsumerReference()).isEqualTo(consumerReference);
        assertThat(instructMessage.getMessageHeader()).isEqualTo(messageHeader);
        assertThat(instructMessage.getHoReference()).isEqualTo(someDocumentReference);
        if (ftpaDecisionOutcome.contains("reheard")) {
            assertThat(instructMessage.getNote())
                .contains(FtpaAppealDecidedNote.fromId("reheard_" + applicantType).getValue());
        } else if (ftpaDecisionOutcome.contains("remade")) {
            assertThat(instructMessage.getNote())
                .contains(FtpaAppealDecidedNote.fromId("remade_" + remadeDecision).getValue());
        } else {
            assertThat(instructMessage.getNote())
                .contains(FtpaAppealDecidedNote.fromId(ftpaDecisionOutcome + "_" + applicantType).getValue());
        }

        assertThat(instructMessage.getMessageType()).isEqualTo(MessageType.COURT_OUTCOME.name());
        assertThat(instructMessage.getCourtOutcome().getCourtType()).isEqualTo(CourtType.FIRST_TIER);
        assertThat(instructMessage.getCourtOutcome().getOutcome()).isIn(Arrays.asList(Outcome.values()));
    }

}
