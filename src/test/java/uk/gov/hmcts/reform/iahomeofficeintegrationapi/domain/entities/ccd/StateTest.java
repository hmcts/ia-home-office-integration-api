package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class StateTest {

    @Test
    void has_correct_values() {
        assertThat(State.APPEAL_STARTED.toString()).isEqualTo("appealStarted");
        assertThat(State.APPEAL_STARTED_BY_ADMIN.toString()).isEqualTo("appealStartedByAdmin");
        assertThat(State.APPEAL_SUBMITTED.toString()).isEqualTo("appealSubmitted");
        assertThat(State.APPEAL_SUBMITTED_OUT_OF_TIME.toString()).isEqualTo("appealSubmittedOutOfTime");
        assertThat(State.AWAITING_RESPONDENT_EVIDENCE.toString()).isEqualTo("awaitingRespondentEvidence");
        assertThat(State.CASE_BUILDING.toString()).isEqualTo("caseBuilding");
        assertThat(State.CASE_UNDER_REVIEW.toString()).isEqualTo("caseUnderReview");
        assertThat(State.RESPONDENT_REVIEW.toString()).isEqualTo("respondentReview");
        assertThat(State.SUBMIT_HEARING_REQUIREMENTS.toString()).isEqualTo("submitHearingRequirements");
        assertThat(State.LISTING.toString()).isEqualTo("listing");
        assertThat(State.PREPARE_FOR_HEARING.toString()).isEqualTo("prepareForHearing");
        assertThat(State.FINAL_BUNDLING.toString()).isEqualTo("finalBundling");
        assertThat(State.PRE_HEARING.toString()).isEqualTo("preHearing");
        assertThat(State.HEARING_AND_OUTCOME.toString()).isEqualTo("hearingAndOutcome");
        assertThat(State.DECIDED.toString()).isEqualTo("decided");
        assertThat(State.UNKNOWN.toString()).isEqualTo("unknown");
        assertThat(State.AWAITING_REASONS_FOR_APPEAL.toString()).isEqualTo("awaitingReasonsForAppeal");
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertThat(State.values().length).isEqualTo(18);
    }
}