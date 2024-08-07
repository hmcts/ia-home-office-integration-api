package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AppealDecidedNote {

    ALLOWED("""
            The grounds for this decision are explained in the Decision and Reasons document.
            
            Next steps
            Visit the online service and use the reference given in this email to find the case.
            You can then view and download the Decision and Reasons document from the Documents tab.
            If you think there's a legal error in the Tribunal's decision,
            you can ask for permission to appeal to the Upper Tribunal (Immigration and Asylum Chamber).
            """),

    DISMISSED("""
              The grounds for this decision are explained in the Decision and Reasons document.
              
              Next steps
              Visit the online service and use the reference given in this email to find the case. 
              You can then view and download the Decision and Reasons document from the Documents tab.
              If the appellant's legal representative thinks there's a legal error in the Tribunal's decision, 
              they may apply for permission to appeal to the Upper Tribunal (Immigration and Asylum Chamber).
              The Home Office will be notified if the appellant asks for permission to appeal.
              """)
    ;

    @JsonValue
    private final String value;

    AppealDecidedNote(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

}
