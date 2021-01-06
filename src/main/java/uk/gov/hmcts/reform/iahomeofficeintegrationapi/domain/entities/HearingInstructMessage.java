package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

public class HearingInstructMessage extends HomeOfficeInstruct {

    private Hearing hearing;

    private HearingInstructMessage() {
    }

    public HearingInstructMessage(
        ConsumerReference consumerReference,
        String hoReference,
        MessageHeader messageHeader,
        String messageType,
        String note,
        Hearing hearing) {

        super(consumerReference, hoReference, messageHeader, messageType, note);
        this.hearing = hearing;
    }

    public Hearing getHearing() {
        return hearing;
    }

    public static final class HearingInstructMessageBuilder {

        private Hearing hearing;
        private ConsumerReference consumerReference;
        private String hoReference;
        private MessageHeader messageHeader;
        private String messageType;
        private String note;

        private HearingInstructMessageBuilder() {
        }

        public static HearingInstructMessageBuilder hearingInstructMessage() {
            return new HearingInstructMessageBuilder();
        }

        public HearingInstructMessageBuilder withHearing(Hearing hearing) {
            this.hearing = hearing;
            return this;
        }

        public HearingInstructMessageBuilder withConsumerReference(ConsumerReference consumerReference) {
            this.consumerReference = consumerReference;
            return this;
        }

        public HearingInstructMessageBuilder withHoReference(String hoReference) {
            this.hoReference = hoReference;
            return this;
        }

        public HearingInstructMessageBuilder withMessageHeader(MessageHeader messageHeader) {
            this.messageHeader = messageHeader;
            return this;
        }

        public HearingInstructMessageBuilder withMessageType(String messageType) {
            this.messageType = messageType;
            return this;
        }

        public HearingInstructMessageBuilder withNote(String note) {
            this.note = note;
            return this;
        }

        public HearingInstructMessage build() {
            return
                new HearingInstructMessage(consumerReference, hoReference, messageHeader, messageType, note, hearing);
        }
    }
}
