package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import static junit.framework.TestCase.assertEquals;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.IdValue;

class DirectionTest {

    private final String explanation = "Do the thing";
    private final Parties parties = Parties.RESPONDENT;
    private final String dateDue = "2018-12-31T12:34:56";
    private final String dateSent = "2018-12-25";
    private DirectionTag tag = DirectionTag.LEGAL_REPRESENTATIVE_REVIEW;
    private List<IdValue<PreviousDates>> previousDates = Collections.emptyList();
    private List<IdValue<ClarifyingQuestion>> clarifyingQuestions = Collections.emptyList();
    private final String uniqueId = UUID.randomUUID().toString();
    private final String directionType = "someEventDirectionType";

    private Direction direction = new Direction(
        explanation,
        parties,
        dateDue,
        dateSent,
        tag,
        previousDates
    );

    private Direction directionWithQuestions = new Direction(
            explanation,
            parties,
            dateDue,
            dateSent,
            tag,
            previousDates,
            clarifyingQuestions,
            uniqueId,
            directionType
    );

    @Test
    void should_hold_onto_values() {

        assertEquals(explanation, direction.getExplanation());
        assertEquals(parties, direction.getParties());
        assertEquals(dateDue, direction.getDateDue());
        assertEquals(dateSent, direction.getDateSent());
        assertEquals(tag, direction.getTag());
        assertEquals(previousDates, direction.getPreviousDates());
    }

    @Test
    void should_hold_onto_values_for_clarifying_questions() {

        Assertions.assertEquals(explanation, directionWithQuestions.getExplanation());
        Assertions.assertEquals(parties, directionWithQuestions.getParties());
        Assertions.assertEquals(dateDue, directionWithQuestions.getDateDue());
        Assertions.assertEquals(dateSent, directionWithQuestions.getDateSent());
        Assertions.assertEquals(tag, directionWithQuestions.getTag());
        Assertions.assertEquals(previousDates, directionWithQuestions.getPreviousDates());
        Assertions.assertEquals(clarifyingQuestions, directionWithQuestions.getClarifyingQuestions());
        Assertions.assertEquals(uniqueId, directionWithQuestions.getUniqueId());
        Assertions.assertEquals(directionType, directionWithQuestions.getDirectionType());
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> new Direction(null, parties, dateDue, dateSent, tag, previousDates))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new Direction(explanation, null, dateDue, dateSent, tag, previousDates))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new Direction(explanation, parties, null, dateSent, tag, previousDates))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new Direction(explanation, parties, dateDue, null, tag, previousDates))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new Direction(explanation, parties, dateDue, dateSent, null, previousDates))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new Direction(explanation, parties, dateDue, dateSent, tag, null))
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
