package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Event {
    SUBMIT_APPEAL("submitAppeal"),
    PAY_AND_SUBMIT_APPEAL("payAndSubmitAppeal"),
    MARK_APPEAL_PAID("markAppealPaid"),
    REQUEST_HOME_OFFICE_DATA("requestHomeOfficeData"),
    REQUEST_RESPONDENT_EVIDENCE("requestRespondentEvidence"),
    REQUEST_RESPONDENT_REVIEW("requestRespondentReview"),
    LIST_CASE("listCase"),
    EDIT_CASE_LISTING("editCaseListing"),
    ADJOURN_HEARING_WITHOUT_DATE("adjournHearingWithoutDate"),
    SEND_DECISION_AND_REASONS("sendDecisionAndReasons"),
    ASYNC_STITCHING_COMPLETE("asyncStitchingComplete"),
    APPLY_FOR_FTPA_APPELLANT("applyForFTPAAppellant"),
    APPLY_FOR_FTPA_RESPONDENT("applyForFTPARespondent"),
    LEADERSHIP_JUDGE_FTPA_DECISION("leadershipJudgeFtpaDecision"),
    RESIDENT_JUDGE_FTPA_DECISION("residentJudgeFtpaDecision"),
    END_APPEAL("endAppeal"),
    SEND_DIRECTION("sendDirection"),
    REQUEST_RESPONSE_AMEND("requestResponseAmend"),
    CHANGE_DIRECTION_DUE_DATE("changeDirectionDueDate"),
    DECIDE_FTPA_APPLICATION("decideFtpaApplication"),
    SET_HOME_OFFICE_STATUTORY_TIMEFRAME_STATUS("addStatutoryTimeframe24Weeks"),//addCaseNote addStatutoryTimeframe24Weeks updateStatutoryTimeframe24Weeks
    @JsonEnumDefaultValue
    UNKNOWN("unknown");

    @JsonValue
    private final String id;

    Event(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }

}
