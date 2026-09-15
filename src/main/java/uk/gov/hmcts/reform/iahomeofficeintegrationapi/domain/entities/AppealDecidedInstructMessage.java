package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

public class AppealDecidedInstructMessage extends HomeOfficeInstruct {

    private CourtOutcome courtOutcome;

    private AppealDecidedInstructMessage() {
    }

    public AppealDecidedInstructMessage(
        ConsumerReference consumerReference,
        String hoReference,
        MessageHeader messageHeader,
        String messageType,
        CourtOutcome courtOutcome,
        String note) {

        super(consumerReference, hoReference, messageHeader, messageType, note);

        this.courtOutcome = courtOutcome;
    }

    public CourtOutcome getCourtOutcome() {
        return courtOutcome;
    }


    public static final class AppealDecidedInstructMessageBuilder {
        private CourtOutcome courtOutcome;
        private ConsumerReference consumerReference;
        private String hoReference;
        private MessageHeader messageHeader;
        private String messageType;
        private String note;

        private AppealDecidedInstructMessageBuilder() {
        }

        public static AppealDecidedInstructMessageBuilder appealDecidedInstructMessage() {
            return new AppealDecidedInstructMessageBuilder();
        }

        public AppealDecidedInstructMessageBuilder withCourtOutcome(CourtOutcome courtOutcome) {
            this.courtOutcome = courtOutcome;
            return this;
        }

        public AppealDecidedInstructMessageBuilder withConsumerReference(ConsumerReference consumerReference) {
            this.consumerReference = consumerReference;
            return this;
        }

        public AppealDecidedInstructMessageBuilder withHoReference(String hoReference) {
            this.hoReference = hoReference;
            return this;
        }

        public AppealDecidedInstructMessageBuilder withMessageHeader(MessageHeader messageHeader) {
            this.messageHeader = messageHeader;
            return this;
        }

        public AppealDecidedInstructMessageBuilder withMessageType(String messageType) {
            this.messageType = messageType;
            return this;
        }

        public AppealDecidedInstructMessageBuilder withNote(String note) {
            this.note = note;
            return this;
        }

        public AppealDecidedInstructMessage build() {
            return new AppealDecidedInstructMessage(
                consumerReference, hoReference, messageHeader, messageType, courtOutcome, note);
        }
    }
}
