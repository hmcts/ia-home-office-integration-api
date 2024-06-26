package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import static junit.framework.TestCase.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DirectionTagTest {

    @Test
    void has_correct_values() {
        assertEquals("buildCase", DirectionTag.BUILD_CASE.toString());
        assertEquals("caseEdit", DirectionTag.CASE_EDIT.toString());
        assertEquals("legalRepresentativeReview", DirectionTag.LEGAL_REPRESENTATIVE_REVIEW.toString());
        assertEquals("legalRepresentativeHearingRequirements",
            DirectionTag.LEGAL_REPRESENTATIVE_HEARING_REQUIREMENTS.toString());
        assertEquals("respondentEvidence", DirectionTag.RESPONDENT_EVIDENCE.toString());
        assertEquals("respondentReview", DirectionTag.RESPONDENT_REVIEW.toString());
        assertEquals("requestCaseBuilding", DirectionTag.REQUEST_CASE_BUILDING.toString());
        assertEquals("requestResponseReview", DirectionTag.REQUEST_RESPONSE_REVIEW.toString());
        assertEquals("requestResponseAmend", DirectionTag.REQUEST_RESPONSE_AMEND.toString());
        assertEquals("requestReasonsForAppeal", DirectionTag.REQUEST_REASONS_FOR_APPEAL.toString());
        assertEquals("requestClarifyingQuestions", DirectionTag.REQUEST_CLARIFYING_QUESTIONS.toString());
        assertEquals("requestCmaRequirements", DirectionTag.REQUEST_CMA_REQUIREMENTS.toString());
        assertEquals("adaListCase", DirectionTag.ADA_LIST_CASE.toString());
        assertEquals("", DirectionTag.NONE.toString());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(14, DirectionTag.values().length);
    }
}
