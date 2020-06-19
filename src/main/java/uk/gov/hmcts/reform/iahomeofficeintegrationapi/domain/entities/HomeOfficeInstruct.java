package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

public class HomeOfficeInstruct {

    private ConsumerReference consumerReference;
    private CourtOutcome courtOutcome;
    private String hoReference;
    private MessageHeader messageHeader;
    private String messageType;

    private HomeOfficeInstruct() {
    }

    public HomeOfficeInstruct(ConsumerReference consumerReference, CourtOutcome courtOutcome, String hoReference,
                              MessageHeader messageHeader, String messageType) {
        this.consumerReference = consumerReference;
        this.courtOutcome = courtOutcome;
        this.hoReference = hoReference;
        this.messageHeader = messageHeader;
        this.messageType = messageType;
    }

    public ConsumerReference getConsumerReference() {
        return consumerReference;
    }

    public CourtOutcome getCourtOutcome() {
        return courtOutcome;
    }

    public String getHoReference() {
        return hoReference;
    }

    public MessageHeader getMessageHeader() {
        return messageHeader;
    }

    public String getMessageType() {
        return messageType;
    }

}
