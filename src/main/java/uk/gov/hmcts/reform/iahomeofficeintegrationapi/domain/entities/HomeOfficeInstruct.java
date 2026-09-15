package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

public class HomeOfficeInstruct {

    private ConsumerReference consumerReference;
    private String hoReference;
    private MessageHeader messageHeader;
    private String messageType;
    private String note;

    protected HomeOfficeInstruct() {
    }

    public HomeOfficeInstruct(ConsumerReference consumerReference, String hoReference,
                              MessageHeader messageHeader, String messageType, String note) {
        this.consumerReference = consumerReference;
        this.hoReference = hoReference;
        this.messageHeader = messageHeader;
        this.messageType = messageType;
        this.note = note;
    }

    public ConsumerReference getConsumerReference() {
        return consumerReference;
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

    public String getNote() {
        return note;
    }

}
