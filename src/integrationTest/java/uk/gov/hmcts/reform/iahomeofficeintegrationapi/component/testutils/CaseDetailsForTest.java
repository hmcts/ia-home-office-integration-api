package uk.gov.hmcts.reform.iahomeofficeintegrationapi.component.testutils;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Classification;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.State;

@Data
public class CaseDetailsForTest {

    @JsonProperty
    private long id;
    @JsonProperty
    private String jurisdiction;
    @JsonProperty
    private State state;
    @JsonProperty("case_data")
    private AsylumCase caseData;
    @JsonProperty("created_date")
    private String createdDate;
    @JsonProperty("last_modified")
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

    CaseDetailsForTest(long id, String jurisdiction, State state, AsylumCase caseData, String createdDate,
                        LocalDateTime lastModified, Integer lockedBy, Integer securityLevel, Classification securityClassification,
                        String callbackResponseStatus, Integer version) {
        this.id = id;
        this.jurisdiction = jurisdiction;
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

    public static class CaseDetailsForTestBuilder implements Builder<CaseDetailsForTest> {

        public static CaseDetailsForTestBuilder someCaseDetailsWith() {
            return new CaseDetailsForTestBuilder();
        }

        private long id = 1;
        private String jurisdiction = "ia";
        private State state;
        private AsylumCase caseData;
        private String createdDate;
        private LocalDateTime lastModified;
        private Integer lockedBy;
        private Integer securityLevel;
        private Classification securityClassification;
        private String callbackResponseStatus;
        private Integer version;

        CaseDetailsForTestBuilder() {
        }

        public CaseDetailsForTestBuilder id(long id) {
            this.id = id;
            return this;
        }

        public CaseDetailsForTestBuilder jurisdiction(String jurisdiction) {
            this.jurisdiction = jurisdiction;
            return this;
        }

        public CaseDetailsForTestBuilder state(State state) {
            this.state = state;
            return this;
        }

        public CaseDetailsForTestBuilder caseData(AsylumCaseForTest caseData) {
            this.caseData = caseData.build();
            return this;
        }

        public CaseDetailsForTestBuilder createdDate(String createdDate) {
            this.createdDate = createdDate;
            return this;
        }

        public CaseDetailsForTestBuilder lastModified(LocalDateTime lastModified) {
            this.lastModified = lastModified;
            return this;
        }

        public CaseDetailsForTestBuilder lockedBy(Integer lockedBy) {
            this.lockedBy = lockedBy;
            return this;
        }

        public CaseDetailsForTestBuilder securityLevel(Integer securityLevel) {
            this.securityLevel = securityLevel;
            return this;
        }

        public CaseDetailsForTestBuilder securityClassification(Classification securityClassification) {
            this.securityClassification = securityClassification;
            return this;
        }

        public CaseDetailsForTestBuilder callbackResponseStatus(String callbackResponseStatus) {
            this.callbackResponseStatus = callbackResponseStatus;
            return this;
        }

        public CaseDetailsForTestBuilder version(Integer version) {
            this.version = version;
            return this;
        }

        public CaseDetailsForTest build() {
            return new CaseDetailsForTest(
                id, jurisdiction, state, caseData, createdDate, lastModified, lockedBy, 
                securityLevel, securityClassification, callbackResponseStatus, version
            );
        }

        public String toString() {
            return "CaseDetailsForTest.CaseDetailsForTestBuilder(id="
                   + this.id + ", jurisdiction="
                   + this.jurisdiction + ", state="
                   + this.state + ", caseData="
                   + this.caseData + ", createdDate="
                   + this.createdDate + ", lastModified="
                   + this.lastModified + ", lockedBy="
                   + this.lockedBy + ", securityLevel="
                   + this.securityLevel + ", securityClassification="
                   + this.securityClassification + ", callbackResponseStatus="
                   + this.callbackResponseStatus + ", version="
                   + this.version + ")";
        }
    }
}
