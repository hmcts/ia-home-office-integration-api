package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.NationalityFieldValue;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.IdValue;

public enum AsylumCaseDefinition {

    HOME_OFFICE_REFERENCE_NUMBER(
        "homeOfficeReferenceNumber", new TypeReference<String>() {}),

    APPEAL_REFERENCE_NUMBER(
        "appealReferenceNumber", new TypeReference<String>(){}),

    HOME_OFFICE_CASE_STATUS_DATA(
        "homeOfficeCaseStatusData", new TypeReference<HomeOfficeCaseStatus>() {}),

    HOME_OFFICE_SEARCH_STATUS(
        "homeOfficeSearchStatus", new TypeReference<String>() {}),

    HOME_OFFICE_SEARCH_STATUS_MESSAGE(
        "homeOfficeSearchStatusMessage", new TypeReference<String>() {}),

    APPELLANT_GIVEN_NAMES(
        "appellantGivenNames", new TypeReference<String>(){}),

    APPELLANT_FAMILY_NAME(
        "appellantFamilyName", new TypeReference<String>(){}),

    APPELLANT_DATE_OF_BIRTH(
        "appellantDateOfBirth", new TypeReference<String>() {}),

    APPELLANT_NATIONALITIES(
        "appellantNationalities", new TypeReference<List<IdValue<NationalityFieldValue>>>(){}),

    APPEAL_TYPE(
        "appealType", new TypeReference<String>(){}),

    APPEAL_SUBMISSION_DATE(
        "appealSubmissionDate", new TypeReference<String>() {}),

    SEND_DIRECTION_DATE_DUE(
        "sendDirectionDateDue", new TypeReference<String>(){}),

    DIRECTIONS(
        "directions", new TypeReference<List<IdValue<Direction>>>(){}),

    HOME_OFFICE_INSTRUCT_STATUS(
        "homeOfficeInstructStatus", new TypeReference<String>() {}),

    HOME_OFFICE_REQUEST_REVIEW_INSTRUCT_STATUS(
        "homeOfficeRequestReviewInstructStatus", new TypeReference<String>() {}),

    HOME_OFFICE_REQUEST_EVIDENCE_INSTRUCT_STATUS(
        "homeOfficeRequestEvidenceInstructStatus", new TypeReference<String>() {});

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
