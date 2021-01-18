package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event.RESIDENT_JUDGE_FTPA_DECISION;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.FtpaDecidedNotificationsHelper;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.HomeOfficeInstructService;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.NotificationsHelper;


@Slf4j
@Component
public class ResidentJudgeFtpaDecidedNotificationHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private HomeOfficeInstructService homeOfficeInstructService;
    private NotificationsHelper notificationsHelper;
    private FtpaDecidedNotificationsHelper ftpaDecidedNotificationsHelper;

    public ResidentJudgeFtpaDecidedNotificationHandler(
        HomeOfficeInstructService homeOfficeInstructService,
        NotificationsHelper notificationsHelper,
        FtpaDecidedNotificationsHelper ftpaDecidedNotificationsHelper
    ) {
        this.homeOfficeInstructService = homeOfficeInstructService;
        this.notificationsHelper = notificationsHelper;
        this.ftpaDecidedNotificationsHelper = ftpaDecidedNotificationsHelper;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && (callback.getEvent() == RESIDENT_JUDGE_FTPA_DECISION);
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        ftpaDecidedNotificationsHelper.handleFtpaDecidedNotification(
            asylumCase, notificationsHelper, homeOfficeInstructService, callback.getEvent(), "RJ_");

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
