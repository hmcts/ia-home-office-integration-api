package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd;

import static junit.framework.TestCase.assertEquals;

import org.junit.jupiter.api.Test;


public class StateTest {

    @Test
    public void has_correct_values() {
        assertEquals("appealStarted", State.APPEAL_STARTED.toString());
        assertEquals("appealStartedByAdmin", State.APPEAL_STARTED_BY_ADMIN.toString());
        assertEquals("appealSubmitted", State.APPEAL_SUBMITTED.toString());
        assertEquals("appealSubmittedOutOfTime", State.APPEAL_SUBMITTED_OUT_OF_TIME.toString());
        assertEquals("awaitingRespondentEvidence", State.AWAITING_RESPONDENT_EVIDENCE.toString());
        assertEquals("caseBuilding", State.CASE_BUILDING.toString());
        assertEquals("caseUnderReview", State.CASE_UNDER_REVIEW.toString());
        assertEquals("respondentReview", State.RESPONDENT_REVIEW.toString());
        assertEquals("submitHearingRequirements", State.SUBMIT_HEARING_REQUIREMENTS.toString());
        assertEquals("listing", State.LISTING.toString());
        assertEquals("prepareForHearing", State.PREPARE_FOR_HEARING.toString());
        assertEquals("finalBundling", State.FINAL_BUNDLING.toString());
        assertEquals("preHearing", State.PRE_HEARING.toString());
        assertEquals("hearingAndOutcome", State.HEARING_AND_OUTCOME.toString());
        assertEquals("decided", State.DECIDED.toString());
        assertEquals("unknown", State.UNKNOWN.toString());
        assertEquals("awaitingReasonsForAppeal", State.AWAITING_REASONS_FOR_APPEAL.toString());
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(18, State.values().length);
    }
}
