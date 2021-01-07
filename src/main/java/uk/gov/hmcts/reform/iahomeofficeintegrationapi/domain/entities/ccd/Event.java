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
