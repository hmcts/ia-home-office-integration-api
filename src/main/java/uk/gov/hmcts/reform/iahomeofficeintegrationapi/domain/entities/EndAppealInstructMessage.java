package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

public class EndAppealInstructMessage extends HomeOfficeInstruct {

    private String endReason;
    private String endChallengeDate;

    private EndAppealInstructMessage() {
    }

    public EndAppealInstructMessage(
        ConsumerReference consumerReference,
        String hoReference,
        MessageHeader messageHeader,
        String messageType,
        String endReason,
        String endChallengeDate,
        String note) {

        super(consumerReference, hoReference, messageHeader, messageType, note);

        this.endReason = endReason;
        this.endChallengeDate = endChallengeDate;
    }

    public String getEndReason() {
        return endReason;
    }

    public String getEndChallengeDate() {
        return endChallengeDate;
    }

    public static final class EndAppealInstructMessageBuilder {
        private String endReason;
        private String endChallengeDate;
        private ConsumerReference consumerReference;
        private String hoReference;
        private MessageHeader messageHeader;
        private String messageType;
        private String note;

        private EndAppealInstructMessageBuilder() {
        }

        public static EndAppealInstructMessageBuilder endAppealInstructMessage() {
            return new EndAppealInstructMessageBuilder();
        }

        public EndAppealInstructMessageBuilder withEndReason(String endReason) {
            this.endReason = endReason;
            return this;
        }

        public EndAppealInstructMessageBuilder withEndChallengeDate(String endChallengeDate) {
            this.endChallengeDate = endChallengeDate;
            return this;
        }


        public EndAppealInstructMessageBuilder withConsumerReference(ConsumerReference consumerReference) {
            this.consumerReference = consumerReference;
            return this;
        }

        public EndAppealInstructMessageBuilder withHoReference(String hoReference) {
            this.hoReference = hoReference;
            return this;
        }

        public EndAppealInstructMessageBuilder withMessageHeader(MessageHeader messageHeader) {
            this.messageHeader = messageHeader;
            return this;
        }

        public EndAppealInstructMessageBuilder withMessageType(String messageType) {
            this.messageType = messageType;
            return this;
        }

        public EndAppealInstructMessageBuilder withNote(String note) {
            this.note = note;
            return this;
        }

        public EndAppealInstructMessage build() {

            return new EndAppealInstructMessage(
                    consumerReference, hoReference, messageHeader, messageType, endReason, endChallengeDate, note);
        }
    }
}
