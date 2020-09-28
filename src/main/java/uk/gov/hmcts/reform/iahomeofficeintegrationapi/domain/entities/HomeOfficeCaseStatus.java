package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

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
    private String displayAppellantDetailsTitle;
    private String displayApplicationDetailsTitle;


    public HomeOfficeCaseStatus(Person person, ApplicationStatus applicationStatus) {
        this.person = person;
        this.applicationStatus = applicationStatus;
    }

    public HomeOfficeCaseStatus(Person person,
                                ApplicationStatus applicationStatus,
                                String displayDateOfBirth,
                                String displayRejectionReasons,
                                String displayDecisionDate,
                                String displayDecisionSentDate,
                                String displayMetadataValueBoolean,
                                String displayMetadataValueDateTime,
                                String displayAppellantDetailsTitle,
                                String displayApplicationDetailsTitle) {
        this.person = person;
        this.applicationStatus = applicationStatus;
        this.displayDateOfBirth = displayDateOfBirth;
        this.displayRejectionReasons = displayRejectionReasons;
        this.displayDecisionDate = displayDecisionDate;
        this.displayDecisionSentDate = displayDecisionSentDate;
        this.displayMetadataValueBoolean = displayMetadataValueBoolean;
        this.displayMetadataValueDateTime = displayMetadataValueDateTime;
        this.displayAppellantDetailsTitle = displayAppellantDetailsTitle;
        this.displayApplicationDetailsTitle = displayApplicationDetailsTitle;
    }

    private HomeOfficeCaseStatus() {

    }

    public Person getPerson() {
        return person;
    }

    public ApplicationStatus getApplicationStatus() {
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

    public String getDisplayAppellantDetailsTitle() {
        return displayAppellantDetailsTitle;
    }

    public String getDisplayApplicationDetailsTitle() {
        return displayApplicationDetailsTitle;
    }

}
