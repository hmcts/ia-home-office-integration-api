package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.DIRECTION_EDIT_DATE_DUE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.DIRECTION_EDIT_EXPLANATION;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.DIRECTION_EDIT_PARTIES;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_EVIDENCE_CHANGE_DIRECTION_DUE_DATE_INSTRUCT_STATUS;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REVIEW_CHANGE_DIRECTION_DUE_DATE_INSTRUCT_STATUS;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.MessageType.DEFAULT;

import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeInstruct;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.Parties;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.HomeOfficeInstructService;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.NotificationsHelper;

@Slf4j
@Component
public class ChangeDirectionDueDateNotificationHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private HomeOfficeInstructService homeOfficeInstructService;
    private NotificationsHelper notificationsHelper;

    public ChangeDirectionDueDateNotificationHandler(
        HomeOfficeInstructService homeOfficeInstructService,
        NotificationsHelper notificationsHelper
    ) {
        this.homeOfficeInstructService = homeOfficeInstructService;
        this.notificationsHelper = notificationsHelper;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.CHANGE_DIRECTION_DUE_DATE
               && isDirectionForRespondentParties(callback.getCaseDetails().getCaseData())
               && (Arrays.asList(
                    State.AWAITING_RESPONDENT_EVIDENCE,
                    State.RESPONDENT_REVIEW
                ).contains(callback.getCaseDetails().getState()));
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }
        log.info("Preparing to send {} notification to HomeOffice for event {}",
            DEFAULT.toString(), callback.getEvent());

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        final String homeOfficeReferenceNumber = notificationsHelper.getHomeOfficeReference(asylumCase);

        final String caseId = notificationsHelper.getCaseId(asylumCase);

        final HomeOfficeInstruct homeOfficeInstruct =
            new HomeOfficeInstruct(
                notificationsHelper.getConsumerReference(caseId),
                homeOfficeReferenceNumber,
                notificationsHelper.getMessageHeader(),
                DEFAULT.name(),
                getNote(asylumCase)
            );

        log.info("Finished constructing {} notification request for caseId: {}, HomeOffice reference: {}",
            DEFAULT.toString(), caseId, homeOfficeReferenceNumber);

        final String notificationStatus = homeOfficeInstructService.sendNotification(homeOfficeInstruct);

        if (State.RESPONDENT_REVIEW.equals(callback.getCaseDetails().getState())) {

            asylumCase.write(HOME_OFFICE_REVIEW_CHANGE_DIRECTION_DUE_DATE_INSTRUCT_STATUS, notificationStatus);

        } else if (State.AWAITING_RESPONDENT_EVIDENCE.equals(callback.getCaseDetails().getState())) {

            asylumCase.write(HOME_OFFICE_EVIDENCE_CHANGE_DIRECTION_DUE_DATE_INSTRUCT_STATUS, notificationStatus);

        }

        log.info("SENT: {} notification for caseId: {}, HomeOffice reference: {}, status: {}, event: {}",
            DEFAULT.toString(), caseId, homeOfficeReferenceNumber, notificationStatus, callback.getEvent());

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private String getNote(AsylumCase asylumCase) {

        String dueDateChanged = asylumCase.read(DIRECTION_EDIT_DATE_DUE, String.class)
            .orElseThrow(() -> new IllegalStateException("Direction Edit Date Due for the appeal is not present"));

        String explanation = asylumCase.read(DIRECTION_EDIT_EXPLANATION, String.class)
            .orElseThrow(() -> new IllegalStateException("Direction Edit Explanation for the appeal is not present"));

        return "The due date for this direction has changed to " + dueDateChanged + "\n" + explanation + "\n";
    }

    protected boolean isDirectionForRespondentParties(AsylumCase asylumCase) {

        Parties parties = asylumCase.read(DIRECTION_EDIT_PARTIES, Parties.class)
            .orElseThrow(() -> new IllegalStateException("sendDirectionParties is not present"));

        return parties.equals(parties.RESPONDENT);
    }
}
