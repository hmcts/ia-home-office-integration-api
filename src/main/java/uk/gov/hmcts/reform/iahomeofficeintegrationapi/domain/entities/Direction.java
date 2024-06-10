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

    public static class Builder {
        private String explanation;
        private Parties parties;
        private String dateDue;
        private String dateSent;
        private DirectionTag tag;
        private List<IdValue<PreviousDates>> previousDates;
        private List<IdValue<ClarifyingQuestion>> clarifyingQuestions;
        private String uniqueId;
        private String directionType;

        public Builder withExplanation(String explanation) {
            this.explanation = explanation;
            return this;
        }

        public Builder withParties(Parties parties) {
            this.parties = parties;
            return this;
        }

        public Builder withDateDue(String dateDue) {
            this.dateDue = dateDue;
            return this;
        }

        public Builder withDateSent(String dateSent) {
            this.dateSent = dateSent;
            return this;
        }

        public Builder withTag(DirectionTag tag) {
            this.tag = tag;
            return this;
        }

        public Builder withPreviousDates(List<IdValue<PreviousDates>> previousDates) {
            this.previousDates = previousDates;
            return this;
        }

        public Builder withClarifyingQuestions(List<IdValue<ClarifyingQuestion>> clarifyingQuestions) {
            this.clarifyingQuestions = clarifyingQuestions;
            return this;
        }

        public Builder withUniqueId(String uniqueId) {
            this.uniqueId = uniqueId;
            return this;
        }

        public Builder withDirectionType(String directionType) {
            this.directionType = directionType;
            return this;
        }

        public Direction build() {
            Direction direction = new Direction();
            direction.explanation = this.explanation;
            direction.parties = this.parties;
            direction.dateDue = this.dateDue;
            direction.dateSent = this.dateSent;
            direction.tag = this.tag;
            direction.previousDates = this.previousDates;
            direction.clarifyingQuestions = this.clarifyingQuestions;
            direction.uniqueId = this.uniqueId;
            direction.directionType = this.directionType;
            return direction;
        }

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
