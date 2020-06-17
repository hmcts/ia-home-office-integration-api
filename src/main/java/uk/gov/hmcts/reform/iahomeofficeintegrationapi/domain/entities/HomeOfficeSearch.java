package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import java.util.List;

public class HomeOfficeSearch {

    private MessageHeader messageHeader;
    private List<SearchParams> searchParams;

    private HomeOfficeSearch() {
    }

    public HomeOfficeSearch(MessageHeader messageHeader, List<SearchParams> searchParams) {
        this.messageHeader = messageHeader;
        this.searchParams = searchParams;
    }

    public MessageHeader getMessageHeader() {
        return messageHeader;
    }

    public List<SearchParams> getSearchParams() {
        return searchParams;
    }
}
