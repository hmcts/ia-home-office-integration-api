package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

public class RequestEvidenceBundleInstructMessage extends HomeOfficeInstruct {

    private String deadlineDate;
    private HomeOfficeChallenge challenge;

    private RequestEvidenceBundleInstructMessage() {
    }

    public RequestEvidenceBundleInstructMessage(
        ConsumerReference consumerReference,
        String hoReference,
        MessageHeader messageHeader,
        String messageType,
        String deadlineDate,
        HomeOfficeChallenge challenge,
        String note) {

        super(consumerReference, hoReference, messageHeader, messageType, note);

        this.deadlineDate = deadlineDate;
        this.challenge = challenge;
    }

    public String getDeadlineDate() {
        return deadlineDate;
    }

    public HomeOfficeChallenge getChallenge() {
        return challenge;
    }

    public static final class RequestEvidenceBundleInstructMessageBuilder {
        private String deadlineDate;
        private HomeOfficeChallenge challenge;
        private ConsumerReference consumerReference;
        private String hoReference;
        private MessageHeader messageHeader;
        private String messageType;
        private String note;

        private RequestEvidenceBundleInstructMessageBuilder() {
        }

        public static RequestEvidenceBundleInstructMessageBuilder requestEvidenceBundleInstructMessage() {
            return new RequestEvidenceBundleInstructMessageBuilder();
        }

        public RequestEvidenceBundleInstructMessageBuilder withDeadlineDate(String deadlineDate) {
            this.deadlineDate = deadlineDate;
            return this;
        }

        public RequestEvidenceBundleInstructMessageBuilder withChallenge(HomeOfficeChallenge challenge) {
            this.challenge = challenge;
            return this;
        }

        public RequestEvidenceBundleInstructMessageBuilder withConsumerReference(ConsumerReference consumerReference) {
            this.consumerReference = consumerReference;
            return this;
        }

        public RequestEvidenceBundleInstructMessageBuilder withHoReference(String hoReference) {
            this.hoReference = hoReference;
            return this;
        }

        public RequestEvidenceBundleInstructMessageBuilder withMessageHeader(MessageHeader messageHeader) {
            this.messageHeader = messageHeader;
            return this;
        }

        public RequestEvidenceBundleInstructMessageBuilder withMessageType(String messageType) {
            this.messageType = messageType;
            return this;
        }

        public RequestEvidenceBundleInstructMessageBuilder withNote(String note) {
            this.note = note;
            return this;
        }

        public RequestEvidenceBundleInstructMessage build() {

            RequestEvidenceBundleInstructMessage requestEvidenceBundleInstructMessage =
                new RequestEvidenceBundleInstructMessage(
                    consumerReference, hoReference, messageHeader, messageType, deadlineDate, challenge, note);

            requestEvidenceBundleInstructMessage.challenge = this.challenge;

            return requestEvidenceBundleInstructMessage;
        }
    }
}
