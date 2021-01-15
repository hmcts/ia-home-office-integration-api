package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class EventTest {

    @Test
    void has_correct_values() {
        assertEquals("submitAppeal", Event.SUBMIT_APPEAL.toString());
        assertEquals("payAndSubmitAppeal", Event.PAY_AND_SUBMIT_APPEAL.toString());
        assertEquals("markAppealPaid", Event.MARK_APPEAL_PAID.toString());
        assertEquals("requestHomeOfficeData", Event.REQUEST_HOME_OFFICE_DATA.toString());
        assertEquals("requestRespondentEvidence", Event.REQUEST_RESPONDENT_EVIDENCE.toString());
        assertEquals("requestRespondentReview", Event.REQUEST_RESPONDENT_REVIEW.toString());
        assertEquals("listCase", Event.LIST_CASE.toString());
        assertEquals("editCaseListing", Event.EDIT_CASE_LISTING.toString());
        assertEquals("adjournHearingWithoutDate", Event.ADJOURN_HEARING_WITHOUT_DATE.toString());
        assertEquals("sendDecisionAndReasons", Event.SEND_DECISION_AND_REASONS.toString());
        assertEquals("asyncStitchingComplete", Event.ASYNC_STITCHING_COMPLETE.toString());
        assertEquals("applyForFTPAAppellant", Event.APPLY_FOR_FTPA_APPELLANT.toString());
        assertEquals("applyForFTPARespondent", Event.APPLY_FOR_FTPA_RESPONDENT.toString());
        assertEquals("unknown", Event.UNKNOWN.toString());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(14, Event.values().length);
    }
}
