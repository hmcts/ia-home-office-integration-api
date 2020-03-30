package uk.gov.hmcts.reform.hois.domain.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HomeOfficeRequest {
    private String iaHomeOfficeReference;
    @JsonProperty("messageHeader")
    private transient MessageHeader messageHeader;
    @JsonProperty("searchParams")
    private transient SearchParameters[] searchParameters;

    public String getIaHomeOfficeReference() {
        return iaHomeOfficeReference;
    }

    public void setIaHomeOfficeReference(String iaHomeOfficeReference) {
        this.iaHomeOfficeReference = iaHomeOfficeReference;
    }

    public MessageHeader getMessageHeader() {
        return messageHeader;
    }

    public void setMessageHeader(MessageHeader messageHeader) {
        this.messageHeader = messageHeader;
    }

    public SearchParameters[] getSearchParameters() {
        return searchParameters;
    }

    public void setSearchParameters(SearchParameters[] searchParameters) {
        this.searchParameters = searchParameters;
    }
}

