package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import com.fasterxml.jackson.core.type.TypeReference;

public enum AsylumCaseDefinition {

    HOME_OFFICE_REFERENCE_NUMBER(
        "homeOfficeReferenceNumber", new TypeReference<String>() {}),

    APPELLANT_GIVEN_NAMES(
        "appellantGivenNames", new TypeReference<String>() {}),

    APPELLANT_FAMILY_NAME(
        "appellantFamilyName", new TypeReference<String>() {}),

    APPEAL_REFERENCE_NUMBER(
        "appealReferenceNumber", new TypeReference<String>(){}),

    HO_APPELLANT_GIVEN_NAME(
        "hoAppellantGivenName", new TypeReference<String>() {}),

    HO_APPELLANT_FAMILY_NAME(
        "hoAppellantFamilyName", new TypeReference<String>() {}),

    HO_APPELLANT_NATIONALITY_CODE(
        "hoAppellantNationalityCode", new TypeReference<String>() {}),

    HO_APPELLANT_NATIONALITY(
        "hoAppellantNationality", new TypeReference<String>() {});

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
