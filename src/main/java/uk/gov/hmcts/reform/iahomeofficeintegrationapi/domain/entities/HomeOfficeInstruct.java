package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import lombok.Getter;

@Getter
public class HomeOfficeInstruct {

    private ConsumerReference consumerReference;
    private String hoReference;
    private MessageHeader messageHeader;
    private String messageType;
    private String note;
    private String pp;

    protected HomeOfficeInstruct() {
    }

    public HomeOfficeInstruct(ConsumerReference consumerReference, String hoReference,
                              MessageHeader messageHeader, String messageType, String note, String pp) {
        this.consumerReference = consumerReference;
        this.hoReference = hoReference;
        this.messageHeader = messageHeader;
        this.messageType = messageType;
        this.note = note;
        this.pp = pp;
    }
}
