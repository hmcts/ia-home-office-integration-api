package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.IdValue;

public class ApplicationStatus {

    private CodeWithDescription applicationType;
    private CodeWithDescription claimReasonType;
    private DecisionCommunication decisionCommunication;
    private String decisionDate;
    private CodeWithDescription decisionType;
    private String documentReference;
    private CodeWithDescription roleSubType;
    private CodeWithDescription roleType;
    @JsonProperty("metadata")
    private List<HomeOfficeMetadata> homeOfficeMetadata;
    private List<RejectionReason> rejectionReasons;
    private List<IdValue<HomeOfficeMetadata>> ccdHomeOfficeMetadata;
    private List<IdValue<RejectionReason>> ccdRejectionReasons;

    private ApplicationStatus() {
    }

    public static class Builder {
        private CodeWithDescription applicationType;
        private CodeWithDescription claimReasonType;
        private DecisionCommunication decisionCommunication;
        private String decisionDate;
        private CodeWithDescription decisionType;
        private String documentReference;
        private CodeWithDescription roleSubType;
        private CodeWithDescription roleType;
        private List<HomeOfficeMetadata> homeOfficeMetadata;
        private List<RejectionReason> rejectionReasons;

        public Builder withApplicationType(CodeWithDescription applicationType) {
            this.applicationType = applicationType;
            return this;
        }

        public Builder withClaimReasonType(CodeWithDescription claimReasonType) {
            this.claimReasonType = claimReasonType;
            return this;
        }

        public Builder withDecisionCommunication(DecisionCommunication decisionCommunication) {
            this.decisionCommunication = decisionCommunication;
            return this;
        }

        public Builder withDecisionDate(String decisionDate) {
            this.decisionDate = decisionDate;
            return this;
        }

        public Builder withDecisionType(CodeWithDescription decisionType) {
            this.decisionType = decisionType;
            return this;
        }

        public Builder withDocumentReference(String documentReference) {
            this.documentReference = documentReference;
            return this;
        }

        public Builder withRoleSubType(CodeWithDescription roleSubType) {
            this.roleSubType = roleSubType;
            return this;
        }

        public Builder withRoleType(CodeWithDescription roleType) {
            this.roleType = roleType;
            return this;
        }

        public Builder withHomeOfficeMetadata(List<HomeOfficeMetadata> homeOfficeMetadata) {
            this.homeOfficeMetadata = homeOfficeMetadata;
            return this;
        }

        public Builder withRejectionReasons(List<RejectionReason> rejectionReasons) {
            this.rejectionReasons = rejectionReasons;
            return this;
        }

        public ApplicationStatus build() {
            ApplicationStatus applicationStatus = new ApplicationStatus();
            applicationStatus.applicationType = this.applicationType;
            applicationStatus.claimReasonType = this.claimReasonType;
            applicationStatus.decisionCommunication = this.decisionCommunication;
            applicationStatus.decisionDate = this.decisionDate;
            applicationStatus.decisionType = this.decisionType;
            applicationStatus.documentReference = this.documentReference;
            applicationStatus.roleSubType = this.roleSubType;
            applicationStatus.roleType = this.roleType;
            applicationStatus.homeOfficeMetadata = this.homeOfficeMetadata;
            applicationStatus.rejectionReasons = this.rejectionReasons;
            return applicationStatus;
        }
    }

    public CodeWithDescription getDecisionType() {
        return decisionType;
    }

    public String getDecisionDate() {
        return decisionDate;
    }

    public CodeWithDescription getApplicationType() {
        return applicationType;
    }

    public CodeWithDescription getClaimReasonType() {
        return claimReasonType;
    }

    public DecisionCommunication getDecisionCommunication() {
        return decisionCommunication;
    }

    public String getDocumentReference() {
        return documentReference;
    }

    public CodeWithDescription getRoleSubType() {
        return roleSubType;
    }

    public CodeWithDescription getRoleType() {
        return roleType;
    }

    public List<HomeOfficeMetadata> getHomeOfficeMetadata() {
        return homeOfficeMetadata;
    }

    public List<RejectionReason> getRejectionReasons() {
        return rejectionReasons;
    }

    public List<IdValue<HomeOfficeMetadata>> getCcdHomeOfficeMetadata() {
        return ccdHomeOfficeMetadata;
    }

    public List<IdValue<RejectionReason>> getCcdRejectionReasons() {
        return ccdRejectionReasons;
    }
}

