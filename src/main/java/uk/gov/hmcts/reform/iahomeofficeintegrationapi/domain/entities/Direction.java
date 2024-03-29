package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.IdValue;

@EqualsAndHashCode
@ToString
public class Direction {

    private String explanation;
    private String uniqueId;
    private String directionType;
    private Parties parties;
    private String dateDue;
    private String dateSent;
    private DirectionTag tag;
    private List<IdValue<PreviousDates>> previousDates;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<IdValue<ClarifyingQuestion>> clarifyingQuestions;

    private Direction() {
        // noop -- for deserializer
    }

    public Direction(
        String explanation,
        Parties parties,
        String dateDue,
        String dateSent,
        DirectionTag tag,
        List<IdValue<PreviousDates>> previousDates,
        List<IdValue<ClarifyingQuestion>> clarifyingQuestions,
        String uniqueId,
        String directionType
    ) {
        requireNonNull(explanation);
        requireNonNull(parties);
        requireNonNull(dateDue);
        requireNonNull(dateSent);
        requireNonNull(tag);
        requireNonNull(previousDates);

        this.explanation = explanation;
        this.parties = parties;
        this.dateDue = dateDue;
        this.dateSent = dateSent;
        this.tag = tag;
        this.previousDates = previousDates;
        this.clarifyingQuestions = clarifyingQuestions;
        this.uniqueId = uniqueId;
        this.directionType = directionType;
    }

    public Direction(
            String explanation,
            Parties parties,
            String dateDue,
            String dateSent,
            DirectionTag tag,
            List<IdValue<PreviousDates>> previousDates,
            List<IdValue<ClarifyingQuestion>> clarifyingQuestions

    ) {
        this(explanation, parties, dateDue, dateSent, tag, previousDates, clarifyingQuestions, null, null);
    }

    public Direction(
            String explanation,
            Parties parties,
            String dateDue,
            String dateSent,
            DirectionTag tag,
            List<IdValue<PreviousDates>> previousDates
    ) {
        this(explanation, parties, dateDue, dateSent, tag, previousDates, null, null, null);
    }

    public String getExplanation() {
        requireNonNull(explanation);
        return explanation;
    }

    public Parties getParties() {
        requireNonNull(parties);
        return parties;
    }

    public String getDateDue() {
        requireNonNull(dateDue);
        return dateDue;
    }

    public String getDateSent() {
        requireNonNull(dateSent);
        return dateSent;
    }

    public DirectionTag getTag() {
        requireNonNull(tag);
        return tag;
    }

    public List<IdValue<PreviousDates>> getPreviousDates() {
        return previousDates;
    }

    public List<IdValue<ClarifyingQuestion>> getClarifyingQuestions() {
        return clarifyingQuestions;
    }


    public String getUniqueId() {
        return uniqueId;
    }

    public String getDirectionType() {
        return directionType;
    }
}
