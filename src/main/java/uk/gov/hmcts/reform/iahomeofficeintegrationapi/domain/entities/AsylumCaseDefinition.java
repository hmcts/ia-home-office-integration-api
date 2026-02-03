package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.DynamicList;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.HearingCentre;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.NationalityFieldValue;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.StatutoryTimeframe24Weeks;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.WitnessDetails;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.HomeOfficeAppellant;

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

    HOME_OFFICE_API_ERROR(
            "homeOfficeApiError", new TypeReference<String>() {}),

    HOME_OFFICE_APPELLANT_API_HTTP_STATUS(
        "homeOfficeAppellantApiHttpStatus", new TypeReference<String>(){}),

    HOME_OFFICE_APPELLANT_CLAIM_DATE(
        "homeOfficeAppellantClaimDate", new TypeReference<String>(){}),

    HOME_OFFICE_APPELLANT_DECISION_DATE(
        "homeOfficeAppellantDecisionDate", new TypeReference<String>() {}),

    HOME_OFFICE_APPELLANT_DECISION_LETTER_DATE(
        "homeOfficeAppellantDecisionLetterDate", new TypeReference<String>(){}),

    HOME_OFFICE_APPELLANTS(
        "homeOfficeAppellants", new TypeReference<List<HomeOfficeAppellant>>(){}),

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

    ADJOURN_HEARING_WITHOUT_DATE_REASONS(
        "adjournHearingWithoutDateReasons", new TypeReference<String>(){}),

    DECISION_HEARING_FEE_OPTION(
        "decisionHearingFeeOption", new TypeReference<String>(){}),

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

    DIRECTION_EDIT_DATE_DUE(
        "directionEditDateDue", new TypeReference<String>(){}),

    DIRECTION_EDIT_EXPLANATION(
        "directionEditExplanation", new TypeReference<String>(){}),

    DIRECTION_EDIT_PARTIES(
        "directionEditParties", new TypeReference<Parties>(){}),

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

    HOME_OFFICE_FTPA_APPELLANT_INSTRUCT_STATUS(
        "homeOfficeFtpaAppellantInstructStatus", new TypeReference<String>() {}),

    HOME_OFFICE_FTPA_RESPONDENT_INSTRUCT_STATUS(
        "homeOfficeFtpaRespondentInstructStatus", new TypeReference<String>() {}),

    HOME_OFFICE_FTPA_APPELLANT_DECIDED_INSTRUCT_STATUS(
        "homeOfficeFtpaAppellantDecidedInstructStatus", new TypeReference<String>() {}),

    HOME_OFFICE_FTPA_RESPONDENT_DECIDED_INSTRUCT_STATUS(
        "homeOfficeFtpaRespondentDecidedInstructStatus", new TypeReference<String>() {}),

    HOME_OFFICE_REQUEST_EVIDENCE_INSTRUCT_STATUS(
        "homeOfficeRequestEvidenceInstructStatus", new TypeReference<String>() {}),

    HOME_OFFICE_HEARING_BUNDLE_READY_INSTRUCT_STATUS(
        "homeOfficeHearingBundleReadyInstructStatus", new TypeReference<String>() {}),

    CASE_FLAG_SET_ASIDE_REHEARD_EXISTS(
        "caseFlagSetAsideReheardExists", new TypeReference<YesOrNo>() {}),

    HOME_OFFICE_END_APPEAL_INSTRUCT_STATUS(
        "homeOfficeEndAppealInstructStatus", new TypeReference<String>() {}),

    END_APPEAL_DATE(
        "endAppealDate", new TypeReference<String>(){}),

    END_APPEAL_OUTCOME(
        "endAppealOutcome", new TypeReference<String>(){}),

    END_APPEAL_OUTCOME_REASON(
        "endAppealOutcomeReason", new TypeReference<String>(){}),

    HOME_OFFICE_AMEND_BUNDLE_INSTRUCT_STATUS(
        "homeOfficeAmendBundleInstructStatus", new TypeReference<String>() {}),

    HOME_OFFICE_AMEND_RESPONSE_INSTRUCT_STATUS(
        "homeOfficeAmendResponseInstructStatus", new TypeReference<String>() {}),

    HOME_OFFICE_REVIEW_CHANGE_DIRECTION_DUE_DATE_INSTRUCT_STATUS(
        "homeOfficeReviewChangeDirectionDueDateInstructStatus", new TypeReference<String>() {}),

    HOME_OFFICE_EVIDENCE_CHANGE_DIRECTION_DUE_DATE_INSTRUCT_STATUS(
        "homeOfficeEvidenceChangeDirectionDueDateInstructStatus", new TypeReference<String>() {}),

    REMOTE_VIDEO_CALL_TRIBUNAL_RESPONSE(
            "remoteVideoCallTribunalResponse", new TypeReference<String>(){}),

    HOME_OFFICE_APPELLANTS_LIST(
            "homeOfficeAppellantsList", new TypeReference<DynamicList>(){}),

    HOME_OFFICE_SEARCH_NO_MATCH(
            "homeOfficeSearchNoMatch", new TypeReference<String>() {}),

    MATCHING_APPELLANT_DETAILS_FOUND(
            "matchingAppellantDetailsFound", new TypeReference<YesOrNo>(){}),

    APPELLANT_FULL_NAME(
            "appellantFullName", new TypeReference<String>(){}),

    HOME_OFFICE_SEARCH_RESPONSE(
            "homeOfficeSearchResponse", new TypeReference<String>() {}),

    HOME_OFFICE_REFERENCE_NUMBER_BEFORE_EDIT(
            "homeOfficeReferenceNumberBeforeEdit", new TypeReference<String>() {}),

    APPEAL_OUT_OF_COUNTRY(
            "appealOutOfCountry", new TypeReference<YesOrNo>() {}),

    STATUTORY_TIMEFRAME_24_WEEKS(
        "statutoryTimeframe24Weeks", new TypeReference<StatutoryTimeframe24Weeks>(){}),

    STF_24W_CURRENT_REASON_AUTO_GENERATED(
            "stf24wCurrentReasonAutoGenerated", new TypeReference<String>() {}),

    STF_24W_HOME_OFFICE_COHORT(
            "stf24wHomeOfficeCohort", new TypeReference<String>() {}),

    STF_24W_CURRENT_STATUS_AUTO_GENERATED(
            "stf24wCurrentStatusAutoGenerated", new TypeReference<YesOrNo>() {})
    ;

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
