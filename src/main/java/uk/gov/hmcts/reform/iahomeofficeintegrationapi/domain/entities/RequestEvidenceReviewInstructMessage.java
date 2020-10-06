package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

public class RequestEvidenceReviewInstructMessage extends HomeOfficeInstruct {

    private String deadlineDate;

    private RequestEvidenceReviewInstructMessage() {
    }

    public RequestEvidenceReviewInstructMessage(
        ConsumerReference consumerReference,
        String hoReference,
        MessageHeader messageHeader,
        String messageType,
        String deadlineDate,
        String note) {

        super(consumerReference, hoReference, messageHeader, messageType, note);

        this.deadlineDate = deadlineDate;
    }

    public String getDeadlineDate() {
        return deadlineDate;
    }


    public static final class RequestEvidenceReviewInstructMessageBuilder {
        private String deadlineDate;
        private ConsumerReference consumerReference;
        private String hoReference;
        private MessageHeader messageHeader;
        private String messageType;
        private String note;

        private RequestEvidenceReviewInstructMessageBuilder() {
        }

        public static RequestEvidenceReviewInstructMessageBuilder requestEvidenceReviewInstructMessage() {
            return new RequestEvidenceReviewInstructMessageBuilder();
        }

        public RequestEvidenceReviewInstructMessageBuilder withDeadlineDate(String deadlineDate) {
            this.deadlineDate = deadlineDate;
            return this;
        }

        public RequestEvidenceReviewInstructMessageBuilder withConsumerReference(ConsumerReference consumerReference) {
            this.consumerReference = consumerReference;
            return this;
        }

        public RequestEvidenceReviewInstructMessageBuilder withHoReference(String hoReference) {
            this.hoReference = hoReference;
            return this;
        }

        public RequestEvidenceReviewInstructMessageBuilder withMessageHeader(MessageHeader messageHeader) {
            this.messageHeader = messageHeader;
            return this;
        }

        public RequestEvidenceReviewInstructMessageBuilder withMessageType(String messageType) {
            this.messageType = messageType;
            return this;
        }

        public RequestEvidenceReviewInstructMessageBuilder withNote(String note) {
            this.note = note;
            return this;
        }

        public RequestEvidenceReviewInstructMessage build() {
            return
                new RequestEvidenceReviewInstructMessage(
                    consumerReference, hoReference, messageHeader, messageType, deadlineDate, note
                );
        }
    }
}
