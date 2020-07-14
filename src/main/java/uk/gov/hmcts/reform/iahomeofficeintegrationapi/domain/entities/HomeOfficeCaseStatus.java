package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import static java.util.Objects.requireNonNull;

public class HomeOfficeCaseStatus {

    private Person person;
    private ApplicationStatus applicationStatus;
    //Added for CCD case data display
    private String displayDateOfBirth;
    private String displayRejectionReasons;
    private String displayDecisionDate;
    private String displayDecisionSentDate;
    private String displayMetadataValueBoolean;
    private String displayMetadataValueDateTime;


    public HomeOfficeCaseStatus(Person person, ApplicationStatus applicationStatus) {
        this.person = person;
        this.applicationStatus = applicationStatus;
    }

    private HomeOfficeCaseStatus() {

    }

    public Person getPerson() {
        requireNonNull(person);
        return person;
    }

    public ApplicationStatus getApplicationStatus() {
        requireNonNull(applicationStatus);
        return applicationStatus;
    }

    public String getDisplayDateOfBirth() {
        return displayDateOfBirth;
    }

    public void setDisplayDateOfBirth(String displayDateOfBirth) {
        this.displayDateOfBirth = displayDateOfBirth;
    }

    public String getDisplayRejectionReasons() {
        return displayRejectionReasons;
    }

    public void setDisplayRejectionReasons(String displayRejectionReasons) {
        this.displayRejectionReasons = displayRejectionReasons;
    }

    public String getDisplayDecisionDate() {
        return displayDecisionDate;
    }

    public void setDisplayDecisionDate(String displayDecisionDate) {
        this.displayDecisionDate = displayDecisionDate;
    }

    public String getDisplayDecisionSentDate() {
        return displayDecisionSentDate;
    }

    public void setDisplayDecisionSentDate(String displayDecisionSentDate) {
        this.displayDecisionSentDate = displayDecisionSentDate;
    }

    public String getDisplayMetadataValueBoolean() {
        return displayMetadataValueBoolean;
    }

    public void setDisplayMetadataValueBoolean(String displayMetadataValueBoolean) {
        this.displayMetadataValueBoolean = displayMetadataValueBoolean;
    }

    public String getDisplayMetadataValueDateTime() {
        return displayMetadataValueDateTime;
    }

    public void setDisplayMetadataValueDateTime(String displayMetadataValueDateTime) {
        this.displayMetadataValueDateTime = displayMetadataValueDateTime;
    }
}
