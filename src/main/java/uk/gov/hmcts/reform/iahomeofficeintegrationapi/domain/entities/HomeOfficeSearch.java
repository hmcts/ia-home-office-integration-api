package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import java.util.Collections;
import java.util.List;

public class HomeOfficeSearch {

    private final MessageHeader messageHeader;
    private final List<SearchParams> searchParams;

    private HomeOfficeSearch() {
        this.messageHeader = null;
        this.searchParams = Collections.emptyList();
    }

    public HomeOfficeSearch(MessageHeader messageHeader, List<SearchParams> searchParams) {
        this.messageHeader = messageHeader;
        this.searchParams = searchParams;
    }

    public MessageHeader getMessageHeader() {
        return messageHeader;
    }

    public List<SearchParams> getSearchParams() {
        return searchParams != null ? Collections.unmodifiableList(searchParams) : Collections.emptyList();
    }
}
