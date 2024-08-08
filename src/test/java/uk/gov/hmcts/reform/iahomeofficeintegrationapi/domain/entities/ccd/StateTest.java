package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class StateTest {

    @Test
    void has_correct_values() {
        assertThat(State.APPEAL_STARTED.toString()).hasToString("appealStarted");
        assertThat(State.APPEAL_STARTED_BY_ADMIN.toString()).hasToString("appealStartedByAdmin");
        assertThat(State.APPEAL_SUBMITTED.toString()).hasToString("appealSubmitted");
        assertThat(State.APPEAL_SUBMITTED_OUT_OF_TIME.toString()).hasToString("appealSubmittedOutOfTime");
        assertThat(State.AWAITING_RESPONDENT_EVIDENCE.toString()).hasToString("awaitingRespondentEvidence");
        assertThat(State.CASE_BUILDING.toString()).hasToString("caseBuilding");
        assertThat(State.CASE_UNDER_REVIEW.toString()).hasToString("caseUnderReview");
        assertThat(State.RESPONDENT_REVIEW.toString()).hasToString("respondentReview");
        assertThat(State.SUBMIT_HEARING_REQUIREMENTS.toString()).hasToString("submitHearingRequirements");
        assertThat(State.LISTING.toString()).hasToString("listing");
        assertThat(State.PREPARE_FOR_HEARING.toString()).hasToString("prepareForHearing");
        assertThat(State.FINAL_BUNDLING.toString()).hasToString("finalBundling");
        assertThat(State.PRE_HEARING.toString()).hasToString("preHearing");
        assertThat(State.HEARING_AND_OUTCOME.toString()).hasToString("hearingAndOutcome");
        assertThat(State.DECIDED.toString()).hasToString("decided");
        assertThat(State.UNKNOWN.toString()).hasToString("unknown");
        assertThat(State.AWAITING_REASONS_FOR_APPEAL.toString()).hasToString("awaitingReasonsForAppeal");
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertThat(State.values()).hasSize(18);
    }
}