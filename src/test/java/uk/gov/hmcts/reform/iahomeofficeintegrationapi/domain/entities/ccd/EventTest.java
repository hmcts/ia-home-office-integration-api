package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class EventTest {
    @ParameterizedTest
    @MethodSource("eventMapping")
    void has_correct_values(String expected, String actual) {
        assertEquals(expected, actual);
    }

    @Test
    void if_this_test_fails_it_is_because_eventMapping_needs_updating_with_your_changes() {
        List<String> eventMappingStrings = eventMapping().map(arg -> arg.get()[1])
            .map(String.class::cast)
            .toList();
        List<Event> missingEvents = Arrays.stream(Event.values())
            .filter(event -> !eventMappingStrings.contains(event.toString())).toList();
        assertTrue(missingEvents.isEmpty(), "The following events are missing from the eventMapping method: " + missingEvents);
    }

    static Stream<Arguments> eventMapping() {
        return Stream.of(
            Arguments.of("startAppeal", Event.START_APPEAL.toString()),
            Arguments.of("editAppeal", Event.EDIT_APPEAL.toString()),
            Arguments.of("submitAppeal", Event.SUBMIT_APPEAL.toString()),
            Arguments.of("payAndSubmitAppeal", Event.PAY_AND_SUBMIT_APPEAL.toString()),
            Arguments.of("markAppealPaid", Event.MARK_APPEAL_PAID.toString()),
            Arguments.of("requestHomeOfficeData", Event.REQUEST_HOME_OFFICE_DATA.toString()),
            Arguments.of("requestRespondentEvidence", Event.REQUEST_RESPONDENT_EVIDENCE.toString()),
            Arguments.of("requestRespondentReview", Event.REQUEST_RESPONDENT_REVIEW.toString()),
            Arguments.of("listCase", Event.LIST_CASE.toString()),
            Arguments.of("editCaseListing", Event.EDIT_CASE_LISTING.toString()),
            Arguments.of("adjournHearingWithoutDate", Event.ADJOURN_HEARING_WITHOUT_DATE.toString()),
            Arguments.of("sendDecisionAndReasons", Event.SEND_DECISION_AND_REASONS.toString()),
            Arguments.of("asyncStitchingComplete", Event.ASYNC_STITCHING_COMPLETE.toString()),
            Arguments.of("applyForFTPAAppellant", Event.APPLY_FOR_FTPA_APPELLANT.toString()),
            Arguments.of("applyForFTPARespondent", Event.APPLY_FOR_FTPA_RESPONDENT.toString()),
            Arguments.of("leadershipJudgeFtpaDecision", Event.LEADERSHIP_JUDGE_FTPA_DECISION.toString()),
            Arguments.of("residentJudgeFtpaDecision", Event.RESIDENT_JUDGE_FTPA_DECISION.toString()),
            Arguments.of("endAppeal", Event.END_APPEAL.toString()),
            Arguments.of("sendDirection", Event.SEND_DIRECTION.toString()),
            Arguments.of("requestResponseAmend", Event.REQUEST_RESPONSE_AMEND.toString()),
            Arguments.of("changeDirectionDueDate", Event.CHANGE_DIRECTION_DUE_DATE.toString()),
            Arguments.of("decideFtpaApplication", Event.DECIDE_FTPA_APPLICATION.toString()),
            Arguments.of("addStatutoryTimeframe24Weeks", Event.SET_HOME_OFFICE_STATUTORY_TIMEFRAME_STATUS.toString()),
            Arguments.of("removeStatutoryTimeframe24Weeks", Event.REMOVE_STATUTORY_TIMEFRAME_24_WEEKS.toString())
        );
    }
}
