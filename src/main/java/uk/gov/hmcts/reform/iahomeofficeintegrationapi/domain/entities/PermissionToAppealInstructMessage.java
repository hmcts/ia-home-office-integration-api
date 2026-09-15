package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

public class PermissionToAppealInstructMessage extends HomeOfficeInstruct {

    private CourtType courtType;

    private PermissionToAppealInstructMessage() {
    }

    public PermissionToAppealInstructMessage(
        ConsumerReference consumerReference,
        String hoReference,
        MessageHeader messageHeader,
        String messageType,
        String note,
        CourtType courtType) {

        super(consumerReference, hoReference, messageHeader, messageType, note);
        this.courtType = courtType;
    }

    public CourtType getCourtType() {
        return courtType;
    }


    public static final class PermissionToAppealInstructMessageBuilder {
        private CourtType courtType;
        private ConsumerReference consumerReference;
        private String hoReference;
        private MessageHeader messageHeader;
        private String messageType;
        private String note;

        private PermissionToAppealInstructMessageBuilder() {
        }

        public static PermissionToAppealInstructMessageBuilder permissionToAppealInstructMessage() {
            return new PermissionToAppealInstructMessageBuilder();
        }

        public PermissionToAppealInstructMessageBuilder withCourtType(CourtType courtType) {
            this.courtType = courtType;
            return this;
        }

        public PermissionToAppealInstructMessageBuilder withConsumerReference(ConsumerReference consumerReference) {
            this.consumerReference = consumerReference;
            return this;
        }

        public PermissionToAppealInstructMessageBuilder withHoReference(String hoReference) {
            this.hoReference = hoReference;
            return this;
        }

        public PermissionToAppealInstructMessageBuilder withMessageHeader(MessageHeader messageHeader) {
            this.messageHeader = messageHeader;
            return this;
        }

        public PermissionToAppealInstructMessageBuilder withMessageType(String messageType) {
            this.messageType = messageType;
            return this;
        }

        public PermissionToAppealInstructMessageBuilder withNote(String note) {
            this.note = note;
            return this;
        }

        public PermissionToAppealInstructMessage build() {
            return
                new PermissionToAppealInstructMessage(
                    consumerReference, hoReference, messageHeader, messageType, note, courtType
                );
        }
    }
}
