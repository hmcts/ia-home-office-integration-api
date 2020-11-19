package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.END_APPEAL_DATE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.END_APPEAL_OUTCOME;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.END_APPEAL_OUTCOME_REASON;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_END_APPEAL_INSTRUCT_STATUS;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.EndAppealInstructMessage.EndAppealInstructMessageBuilder.endAppealInstructMessage;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.MessageType.REQUEST_CHALLENGE_END;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.EndAppealInstructMessage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.EndAppealOutcome;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.HomeOfficeInstructService;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.NotificationsHelper;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.util.HomeOfficeDateFormatter;

@Slf4j
@Component
public class EndAppealNotificationHandler implements PreSubmitCallbackHandler<AsylumCase> {

    protected final HomeOfficeInstructService homeOfficeInstructService;
    protected final NotificationsHelper notificationsHelper;

    public EndAppealNotificationHandler(
        HomeOfficeInstructService homeOfficeInstructService,
        NotificationsHelper notificationsHelper) {
        this.homeOfficeInstructService = homeOfficeInstructService;
        this.notificationsHelper = notificationsHelper;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && (callback.getEvent() == Event.END_APPEAL);
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        log.info("Preparing to send {} notification to HomeOffice for event {}",
            REQUEST_CHALLENGE_END.toString(), callback.getEvent());

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        final String homeOfficeReferenceNumber = notificationsHelper.getHomeOfficeReference(asylumCase);

        final String caseId = notificationsHelper.getCaseId(asylumCase);

        final EndAppealInstructMessage endAppealInstructMessage
            = endAppealInstructMessage()
                .withConsumerReference(notificationsHelper.getConsumerReference(caseId))
                .withHoReference(homeOfficeReferenceNumber)
                .withMessageHeader(notificationsHelper.getMessageHeader())
                .withMessageType(REQUEST_CHALLENGE_END.name())
                .withEndReason(getEndAppealOutcomeName(asylumCase))
                .withEndChallengeDate(HomeOfficeDateFormatter.getIacDateAndTime(getEndAppealDate(asylumCase)))
                .withNote(getEndAppealOutcomeReason(asylumCase))
                .build();

        log.info("Finished constructing {} notification request for caseId: {}, HomeOffice reference: {}",
            REQUEST_CHALLENGE_END.name(), caseId, homeOfficeReferenceNumber);

        final String notificationStatus = homeOfficeInstructService.sendNotification(endAppealInstructMessage);

        log.info("SENT: {} notification for caseId: {}, HomeOffice reference: {}, status: {}, event: {}",
            REQUEST_CHALLENGE_END.name(), caseId, homeOfficeReferenceNumber,
            notificationStatus, callback.getEvent());

        asylumCase.write(HOME_OFFICE_END_APPEAL_INSTRUCT_STATUS, notificationStatus);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private String getEndAppealDate(AsylumCase asylumCase) {
        return asylumCase
            .read(END_APPEAL_DATE, String.class)
            .orElseThrow(() -> new IllegalStateException("endAppealDate is not present"));
    }

    private String getEndAppealOutcome(AsylumCase asylumCase) {
        return asylumCase
            .read(END_APPEAL_OUTCOME, String.class)
            .orElseThrow(() -> new IllegalStateException("endAppealOutcome is not present"));
    }

    private String getEndAppealOutcomeName(AsylumCase asylumCase) {

        Optional<EndAppealOutcome> endAppealOutcomeName
            = EndAppealOutcome.from(getEndAppealOutcome(asylumCase));

        return endAppealOutcomeName.isPresent()
                    ? endAppealOutcomeName.get().name()
                    : EndAppealOutcome.INCORRECT_DETAILS.name();
    }

    private String getEndAppealOutcomeReason(AsylumCase asylumCase) {
        return asylumCase
            .read(END_APPEAL_OUTCOME_REASON, String.class)
            .orElse(null);
    }

}
