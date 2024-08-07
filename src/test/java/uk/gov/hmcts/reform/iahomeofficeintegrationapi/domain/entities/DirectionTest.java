package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

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
    private final DirectionTag tag = DirectionTag.LEGAL_REPRESENTATIVE_REVIEW;
    private final List<IdValue<PreviousDates>> previousDates = Collections.emptyList();
    private final List<IdValue<ClarifyingQuestion>> clarifyingQuestions = Collections.emptyList();
    private final String uniqueId = UUID.randomUUID().toString();
    private final String directionType = "someEventDirectionType";

    private final Direction directionWithQuestions = new Direction.Builder()
            .withExplanation(explanation)
            .withParties(parties)
            .withDateDue(dateDue)
            .withDateSent(dateSent)
            .withTag(tag)
            .withPreviousDates(previousDates)
            .withClarifyingQuestions(clarifyingQuestions)
            .withUniqueId(uniqueId)
            .withDirectionType(directionType)
            .build();

    private final Direction direction = new Direction.Builder()
            .withExplanation(explanation)
            .withParties(parties)
            .withDateDue(dateDue)
            .withDateSent(dateSent)
            .withTag(tag)
            .withPreviousDates(previousDates)
            .build();

    @Test
    void should_hold_onto_values() {

        Assertions.assertEquals(explanation, direction.getExplanation());
        Assertions.assertEquals(parties, direction.getParties());
        Assertions.assertEquals(dateDue, direction.getDateDue());
        Assertions.assertEquals(dateSent, direction.getDateSent());
        Assertions.assertEquals(tag, direction.getTag());
        Assertions.assertEquals(previousDates, direction.getPreviousDates());
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
