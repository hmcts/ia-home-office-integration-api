package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import com.fasterxml.jackson.core.type.TypeReference;

public enum AsylumCaseDefinition {

    HOME_OFFICE_REFERENCE_NUMBER(
        "homeOfficeReferenceNumber", new TypeReference<String>() {}),

    APPEAL_REFERENCE_NUMBER(
        "appealReferenceNumber", new TypeReference<String>(){}),

    HOME_OFFICE_CASE_STATUS_DATA(
        "homeOfficeCaseStatusData", new TypeReference<HomeOfficeCaseStatus>() {}),

    HOME_OFFICE_SEARCH_STATUS(
        "homeOfficeSearchStatus", new TypeReference<String>() {}),

    HOME_OFFICE_INSTRUCT_STATUS(
        "homeOfficeInstructStatus", new TypeReference<String>() {});

    private final String value;
    private final TypeReference typeReference;

    AsylumCaseDefinition(String value, TypeReference typeReference) {
        this.value = value;
        this.typeReference = typeReference;
    }

    public String value() {
        return value;
    }

    public TypeReference getTypeReference() {
        return typeReference;
    }
}
