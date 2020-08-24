package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

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

    private ApplicationStatus() {

    }

    public ApplicationStatus(
        CodeWithDescription applicationType,
        CodeWithDescription claimReasonType,
        DecisionCommunication decisionCommunication,
        String decisionDate,
        CodeWithDescription decisionType,
        String documentReference,
        CodeWithDescription roleSubType,
        CodeWithDescription roleType,
        List<HomeOfficeMetadata> homeOfficeMetadata,
        List<RejectionReason> rejectionReasons) {
        this.applicationType = applicationType;
        this.claimReasonType = claimReasonType;
        this.decisionCommunication = decisionCommunication;
        this.decisionDate = decisionDate;
        this.decisionType = decisionType;
        this.documentReference = documentReference;
        this.roleSubType = roleSubType;
        this.roleType = roleType;
        this.homeOfficeMetadata = homeOfficeMetadata;
        this.rejectionReasons = rejectionReasons;
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

}

