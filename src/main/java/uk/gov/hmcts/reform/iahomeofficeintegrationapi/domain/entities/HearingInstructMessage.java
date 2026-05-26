package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import com.microsoft.applicationinsights.boot.dependencies.apachecommons.lang3.StringUtils;
import lombok.Getter;

@Getter
public class HearingInstructMessage extends HomeOfficeInstruct {

    private Hearing hearing;
    private long ccdCaseId;

    private HearingInstructMessage() {
    }

    public HearingInstructMessage(
        ConsumerReference consumerReference,
        String hoReference,
        MessageHeader messageHeader,
        String messageType,
        String note,
        Hearing hearing,
        long ccdCaseId) {

        super(consumerReference, hoReference, messageHeader, messageType, note);
        this.hearing = hearing;
        this.ccdCaseId = ccdCaseId;
    }

    public static final class HearingInstructMessageBuilder {

        private Hearing hearing;
        private ConsumerReference consumerReference;
        private String hoReference;
        private MessageHeader messageHeader;
        private String messageType;
        private long ccdCaseId;
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

        public HearingInstructMessageBuilder withCcdCaseId(long ccdCaseId) {
            this.ccdCaseId = ccdCaseId;
            return this;
        }

        public HearingInstructMessageBuilder withNote(String note) {
            if (StringUtils.isNotBlank(note)) {
                this.note = note;
            }
            return this;
        }

        public HearingInstructMessage build() {
            return
                new HearingInstructMessage(consumerReference, hoReference, messageHeader, messageType, note, hearing, ccdCaseId);
        }
    }
}
