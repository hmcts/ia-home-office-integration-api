package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

public class SearchParams {

    private String spType;
    private String spValue;

    public SearchParams(String spType, String spValue) {
        this.spType = spType;
        this.spValue = spValue;
    }

    private SearchParams() {

    }

    public String getSpType() {
        return spType;
    }

    public String getSpValue() {
        return spValue;
    }
}
