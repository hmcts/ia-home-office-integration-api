package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.LocalDateTime;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.RequiredFieldMissingException;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CaseDetails<T extends CaseData> {

    private long id;
    private String jurisdiction;
    @JsonProperty("case_type_id")
    @JsonAlias("case_type")
    private String caseTypeId;
    private State state;
    @JsonProperty("case_data")
    @JsonAlias("data")
    private T caseData;
    private LocalDateTime createdDate;
    @JsonProperty("last_modified")
    @JsonAlias("last_modified_on")
    private LocalDateTime lastModified;
    @JsonProperty("locked_by_user_id")
    private Integer lockedBy;
    @JsonProperty("security_level")
    private Integer securityLevel;
    @JsonProperty("security_classification")
    private Classification securityClassification;
    @JsonProperty("callback_response_status")
    private String callbackResponseStatus;
    private Integer version;

    private CaseDetails() {
        // noop -- for deserializer
    }

    public CaseDetails(
        long id,
        String jurisdiction,
        String caseTypeId,
        State state,
        T caseData,
        LocalDateTime createdDate,
        LocalDateTime lastModified,
        Integer lockedBy,
        Integer securityLevel,
        Classification securityClassification,
        String callbackResponseStatus,
        Integer version
    ) {
        this.id = id;
        this.jurisdiction = jurisdiction;
        this.caseTypeId = caseTypeId;
        this.state = state;
        this.caseData = caseData;
        this.createdDate = createdDate;
        this.lastModified = lastModified;
        this.lockedBy = lockedBy;
        this.securityLevel = securityLevel;
        this.securityClassification = securityClassification;
        this.callbackResponseStatus = callbackResponseStatus;
        this.version = version;
    }

    public long getId() {
        return id;
    }

    public String getJurisdiction() {

        if (jurisdiction == null) {
            throw new RequiredFieldMissingException("jurisdiction field is required");
        }

        return jurisdiction;
    }

    public String getCaseTypeId() {
        return caseTypeId;
    }

    public State getState() {

        if (state == null) {
            throw new RequiredFieldMissingException("state field is required");
        }

        return state;
    }

    public T getCaseData() {

        if (caseData == null) {
            throw new RequiredFieldMissingException("caseData field is required");
        }

        return caseData;
    }

    public LocalDateTime getCreatedDate() {

        if (createdDate == null) {
            throw new RequiredFieldMissingException("createdDate field is required");
        }

        return createdDate;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public Integer getLockedBy() {
        return lockedBy;
    }

    public Integer getSecurityLevel() {
        return securityLevel;
    }

    public Classification getSecurityClassification() {
        return securityClassification;
    }

    public String getCallbackResponseStatus() {
        return callbackResponseStatus;
    }

    public Integer getVersion() {
        return version;
    }
}
