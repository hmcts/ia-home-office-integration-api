package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service;

import static java.util.Arrays.stream;

import com.fasterxml.jackson.annotation.JsonValue;

public enum FtpaAppealDecidedNote {

    GRANTED_RESPONDENT(
            "granted_respondent",
            """
            Your application for permission to appeal to the Upper Tribunal has been granted.
        
            The other party has also been notified of this decision.
        
            Next steps
        
            Visit the online service and use the HMCTS reference in this email to find the case.
             The notes in the FTPA tab will explain the Tribunal's decision.The Upper Tribunal will contact you.
            """),

    GRANTED_APPELLANT(
            "granted_appellant",
            """
            The application for permission to appeal to the Upper Tribunal has been granted.
        
            Next steps
        
            Visit the online service and use the HMCTS reference in this email to find the case.
             The notes in the FTPA tab will explain the Tribunal's decision.
        
            The Upper Tribunal will contact you regarding the case.
            """),

    PARTIALLY_GRANTED_RESPONDENT(
            "partiallyGranted_respondent",
            """
            Your application for permission to appeal to the Upper Tribunal has been partially granted.
        
            This means you've been given permission to appeal only on limited grounds.
        
            Next steps
        
            Visit the online service and use the HMCTS reference in this email to find the case.
            The notes in the FTPA tab will explain the Tribunal's decision.The Upper Tribunal will contact you.
        
            You can apply to the Upper Tribunal for permission to appeal on any of the grounds.
            Applicants in the UK have 14 days from the date of this email,
            applicants outside the UK have one month from this date.
        
            The other party has also been notified of this decision.
            """),

    PARTIALLY_GRANTED_APPELLANT(
            "partiallyGranted_appellant",
            """
            The application for permission to appeal to the Upper Tribunal has been partially granted.
        
            Next steps
        
            Visit the online service and use the HMCTS reference in this email to find the case.
            The notes in the FTPA tab will explain the Tribunal's decision.The Upper Tribunal will contact you.
            """),

    REFUSED_RESPONDENT(
            "refused_respondent",
            """
            Your application for permission to appeal to the Upper Tribunal has been refused.
        
            Next steps
        
            Visit the online service and use the HMCTS reference in this email to find the case.
            You can then view and download the FTPA Decision and Reasons document from the FTPA tab.
        
            You can apply to the Upper Tribunal for permission to appeal this decision.
            Applicants in the UK have 14 days from the date of this email,
            applicants outside the UK have one month from this date.
        
            The Form IAUT1- Application for permission to appeal from First-tier Tribunal can be found at
        
            https://www.gov.uk/government/publications/form-iaut1-application-for-permission-to-appeal-from-first-tier-tribunal
            """),

    REFUSED_APPELLANT(
            "refused_appellant",
            """
            The application for permission to appeal to the Upper Tribunal has been refused.
        
            Next steps
        
            Visit the online service and use the HMCTS reference in this email to find the case.
            You can then view and download the FTPA Decision and Reasons document from the FTPA tab.
            """),

    REHEARD_RESPONDENT(
            "reheard_respondent",
            """
            The decision on this case has been set aside and the case will be reheard.
        
            As a result of your application for permission to appeal to the Upper Tribunal,
             the case will be reheard by the First-tier Tribunal.
        
            Next steps
        
            The other party has been notified. The Tribunal will contact you about what happens next.
            """),

    REHEARD_APPELLANT(
            "reheard_appellant",
            """
            The decision on this case has been set aside and the case will be reheard.
        
            As a result of the other party's application for permission to appeal to the Upper Tribunal,
             the case will be reheard by the First-tier Tribunal.
        
            Next steps
        
            The Tribunal will contact you about what happens next.
            """),

    REMADE_ALLOWED(
            "remade_allowed",
            """
            The decision on this case has been set aside and the case will be reheard.
        
            As a result of the other party's application for permission to appeal to the Upper Tribunal,
             the case will be reheard by the First-tier Tribunal.
        
            Next steps
        
            The Tribunal will contact you about what happens next.
            """),

    REMADE_DISMISSED(
            "remade_dismissed",
            """
            The decision on this case has been set aside and the appeal has been dismissed.
        
            As a result of the application for permission to appeal to the Upper Tribunal, the
             decision on this case has been set aside by the First-tier Tribunal and the appeal has been dismissed.
        
            Next steps
        
            Visit the online service and use the reference given in this email to find the case.
             You can then view and download the Decisions and Reasons document from the Documents tab.
        
            If you think there's a legal error in the Tribunal's decision, you can ask for permission
             to appeal to the Upper Tribunal (Immigration and Asylum Chamber) by downloading and completing
             form IAFT- 4: First-tier Tribunal Application for Permission to Appeal to Upper Tribunal from
             the GOV.UK website.
            """),

    NOT_ADMITTED_RESPONDENT(
            "notAdmitted_respondent",
            """
            Your application for permission to appeal to the Upper Tribunal has not been admitted.
        
            Next steps
        
            Visit the online service and use the HMCTS reference in this email to find the case.
            You can then view and download the FTPA Decision and Reasons document from the FTPA tab.
        
            You can apply to the Upper Tribunal for permission to appeal this decision. Applicants in the UK
            have 14 days from the date of this email, applicants outside the UK have one month from this date.
        
            The Form IAUT1- Application for permission to appeal from First-tier Tribunal can be found at
        
            https://www.gov.uk/government/publications/form-iaut1-application-for-permission-to-appeal-from-first-tier-tribunal
            """),

    NOT_ADMITTED_APPELLANT(
            "notAdmitted_appellant",
            """
            The application for permission to appeal to the Upper Tribunal has not been admitted.
        
            Next steps
        
            Visit the online service and use the HMCTS reference in this email to find the case.
            You can then view and download the FTPA Decision and Reasons document from the FTPA tab.
            """);


    @JsonValue
    private final String id;
    @JsonValue
    private final String value;

    FtpaAppealDecidedNote(String id, String value) {
        this.id = id;
        this.value = value;
    }

    public static FtpaAppealDecidedNote fromId(String id) {
        return stream(values())
            .filter(v -> v.getId().equals(id))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(id + " not a FtpaAppealDecidedNote"));
    }

    public String getValue() {
        return value;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return value;
    }

}
