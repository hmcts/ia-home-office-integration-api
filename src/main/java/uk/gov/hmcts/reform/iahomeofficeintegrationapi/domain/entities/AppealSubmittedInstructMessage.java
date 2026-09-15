package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

public class AppealSubmittedInstructMessage extends HomeOfficeInstruct {

    private AppealSubmittedInstructMessage() {
    }

    public AppealSubmittedInstructMessage(
        ConsumerReference consumerReference,
        String hoReference,
        MessageHeader messageHeader,
        String messageType,
        String note) {

        super(consumerReference, hoReference, messageHeader, messageType, note);

    }

    public static final class AppealSubmittedInstructMessageBuilder {
        private ConsumerReference consumerReference;
        private String hoReference;
        private MessageHeader messageHeader;
        private String messageType;
        private String note;

        private AppealSubmittedInstructMessageBuilder() {
        }

        public static AppealSubmittedInstructMessageBuilder appealSubmittedInstructMessage() {
            return new AppealSubmittedInstructMessageBuilder();
        }

        public AppealSubmittedInstructMessageBuilder withConsumerReference(ConsumerReference consumerReference) {
            this.consumerReference = consumerReference;
            return this;
        }

        public AppealSubmittedInstructMessageBuilder withHoReference(String hoReference) {
            this.hoReference = hoReference;
            return this;
        }

        public AppealSubmittedInstructMessageBuilder withMessageHeader(MessageHeader messageHeader) {
            this.messageHeader = messageHeader;
            return this;
        }

        public AppealSubmittedInstructMessageBuilder withMessageType(String messageType) {
            this.messageType = messageType;
            return this;
        }

        public AppealSubmittedInstructMessageBuilder withNote(String note) {
            this.note = note;
            return this;
        }

        public AppealSubmittedInstructMessage build() {
            return new AppealSubmittedInstructMessage(
                consumerReference, hoReference, messageHeader, messageType, note);
        }
    }
}
