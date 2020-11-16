package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.HearingCentre;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.NationalityFieldValue;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.WitnessDetails;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.IdValue;

public enum AsylumCaseDefinition {

    HOME_OFFICE_REFERENCE_NUMBER(
        "homeOfficeReferenceNumber", new TypeReference<String>() {}),

    ALLOWED_NOTE(
        "allowedNote", new TypeReference<String>() {}),

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

    ARIA_LISTING_REFERENCE(
        "ariaListingReference", new TypeReference<String>(){}),

    LIST_CASE_HEARING_CENTRE(
        "listCaseHearingCentre",  new TypeReference<HearingCentre>(){}),

    LIST_CASE_HEARING_DATE(
        "listCaseHearingDate",  new TypeReference<String>(){}),

    WITNESS_COUNT(
        "witnessCount", new TypeReference<String>() {}),

    WITNESS_DETAILS(
        "witnessDetails", new TypeReference<List<IdValue<WitnessDetails>>>() {}),

    VULNERABILITIES_TRIBUNAL_RESPONSE(
        "vulnerabilitiesTribunalResponse", new TypeReference<String>(){}),

    MULTIMEDIA_TRIBUNAL_RESPONSE(
        "multimediaTribunalResponse", new TypeReference<String>(){}),

    SINGLE_SEX_COURT_TRIBUNAL_RESPONSE(
        "singleSexCourtTribunalResponse", new TypeReference<String>(){}),

    IN_CAMERA_COURT_TRIBUNAL_RESPONSE(
        "inCameraCourtTribunalResponse", new TypeReference<String>(){}),

    ADDITIONAL_TRIBUNAL_RESPONSE(
        "additionalTribunalResponse", new TypeReference<String>(){}),

    DECISION_HEARING_FEE_OPTION(
        "decisionHearingFeeOption", new TypeReference<String>(){}),

    ADJOURN_HEARING_WITHOUT_DATE_REASONS(
        "adjournHearingWithoutDateReasons", new TypeReference<String>(){}),

    IS_DECISION_ALLOWED(
        "isDecisionAllowed", new TypeReference<AppealDecision>(){}),

    FTPA_APPLICANT_TYPE(
        "ftpaApplicantType", new TypeReference<String>(){}),

    FTPA_APPELLANT_DECISION_OUTCOME_TYPE(
        "ftpaAppellantDecisionOutcomeType", new TypeReference<String>(){}),

    FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE(
        "ftpaAppellantRjDecisionOutcomeType", new TypeReference<String>(){}),

    FTPA_RESPONDENT_DECISION_OUTCOME_TYPE(
        "ftpaRespondentDecisionOutcomeType", new TypeReference<String>(){}),

    FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE(
        "ftpaRespondentRjDecisionOutcomeType", new TypeReference<String>(){}),

    FTPA_APPELLANT_DECISION_REMADE_RULE_32(
        "ftpaAppellantDecisionRemadeRule32", new TypeReference<String>(){}),

    FTPA_RESPONDENT_DECISION_REMADE_RULE_32(
        "ftpaRespondentDecisionRemadeRule32", new TypeReference<String>(){}),

    HOME_OFFICE_INSTRUCT_STATUS(
        "homeOfficeInstructStatus", new TypeReference<String>() {}),

    HOME_OFFICE_REQUEST_REVIEW_INSTRUCT_STATUS(
        "homeOfficeRequestReviewInstructStatus", new TypeReference<String>() {}),

    HOME_OFFICE_HEARING_INSTRUCT_STATUS(
        "homeOfficeHearingInstructStatus", new TypeReference<String>() {}),

    HOME_OFFICE_EDIT_LISTING_INSTRUCT_STATUS(
        "homeOfficeEditListingInstructStatus", new TypeReference<String>() {}),

    HOME_OFFICE_ADJOURN_WITHOUT_DATE_INSTRUCT_STATUS(
        "homeOfficeAdjournWithoutDateInstructStatus", new TypeReference<String>() {}),

    HOME_OFFICE_APPEAL_DECIDED_INSTRUCT_STATUS(
        "homeOfficeAppealDecidedInstructStatus", new TypeReference<String>() {}),

    HOME_OFFICE_FTPA_APPELLANT_DECIDED_INSTRUCT_STATUS(
        "homeOfficeFtpaAppellantDecidedInstructStatus", new TypeReference<String>() {}),

    HOME_OFFICE_FTPA_RESPONDENT_DECIDED_INSTRUCT_STATUS(
        "homeOfficeFtpaRespondentDecidedInstructStatus", new TypeReference<String>() {}),

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
