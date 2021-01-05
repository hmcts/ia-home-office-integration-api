package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import java.util.List;

public class HomeOfficeChallenge {

    private List<Person> applicants;
    private String appealType;
    private String appealTierType;
    private String challengeSubmissionDate;


    // delete it after implementation complete
    public HomeOfficeChallenge() {
    }

    public HomeOfficeChallenge(List<Person> applicants, String appealType,
                               String appealTierType, String challengeSubmissionDate) {
        this.applicants = applicants;
        this.appealType = appealType;
        this.appealTierType = appealTierType;
        this.challengeSubmissionDate = challengeSubmissionDate;
    }

    public List<Person> getApplicants() {
        return applicants;
    }

    public void setApplicants(List<Person> applicants) {
        this.applicants = applicants;
    }

    public String getAppealType() {
        return appealType;
    }

    public void setAppealType(String appealType) {
        this.appealType = appealType;
    }

    public String getAppealTierType() {
        return appealTierType;
    }

    public void setAppealTierType(String appealTierType) {
        this.appealTierType = appealTierType;
    }

    public String getChallengeSubmissionDate() {
        return challengeSubmissionDate;
    }

    public void setChallengeSubmissionDate(String challengeSubmissionDate) {
        this.challengeSubmissionDate = challengeSubmissionDate;
    }
}
