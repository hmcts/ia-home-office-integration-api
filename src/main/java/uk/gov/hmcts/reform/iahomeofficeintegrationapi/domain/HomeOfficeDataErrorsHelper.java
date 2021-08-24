package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain;

import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_API_ERROR;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition;

@Slf4j
@Component
public class HomeOfficeDataErrorsHelper {

    private static final String PROBLEM_MESSAGE = "### There is a problem\n\n";

    private static final String HOME_OFFICE_CALL_ERROR_MESSAGE = PROBLEM_MESSAGE
            + "The service has been unable t"
            + "o retrieve the Home Office information about this appeal.\n\n"
            + "[Request the Home Office information](/case/IA/Asylum/${[CASE_REFERENCE]}"
            + "/trigger/requestHomeOfficeData) to try again. This may take a few minutes.";

    private static final String HOME_OFFICE_INVALID_REFERENCE_ERROR_MESSAGE = PROBLEM_MESSAGE
            + "The appellant entered the "
            + "Home Office reference number incorrectly. You can contact the appellant to check the reference number"
            + " if you need this information to validate the appeal";

    private static final String HOME_OFFICE_REFERENCE_NOT_FOUND_ERROR_MESSAGE = PROBLEM_MESSAGE
            + "The appellant’s Home Office reference"
            + " number could not be found. You can contact the Home Office to check the reference"
            + " if you need this information to validate the appeal";

    private static final String HOME_OFFICE_APPELLANT_NOT_FOUND_ERROR_MESSAGE = PROBLEM_MESSAGE
            + "The service has been unable to retrieve the Home Office information about this appeal "
            + "because the Home Office reference number does not have any matching appellant data in the system. "
            + "You can contact the Home Office if you need more information to validate the appeal.";

    private static final String INVALID_HOME_OFFICE_REFERENCE = "The Home office does not recognise the submitted "
            + "appellant reference";

    public void setErrorMessageForErrorCode(
            long caseId, AsylumCase asylumCase, String errorCodeStr, String errMessage) {
        final int errorCode = errorCodeStr != null && !errorCodeStr.isEmpty() ? Integer.valueOf(errorCodeStr) : 0;
        getErrorMessage(caseId, asylumCase, errMessage, errorCode, HOME_OFFICE_APPELLANT_NOT_FOUND_ERROR_MESSAGE, log,
                HOME_OFFICE_REFERENCE_NOT_FOUND_ERROR_MESSAGE, HOME_OFFICE_INVALID_REFERENCE_ERROR_MESSAGE,
                HOME_OFFICE_CALL_ERROR_MESSAGE);
    }

    public static void getErrorMessage(long caseId, AsylumCase asylumCase, String errMessage, int errorCode,
                                       String homeOfficeAppellantNotFoundErrorMessage, Logger log,
                                       String homeOfficeReferenceNotFoundErrorMessage,
                                       String homeOfficeInvalidReferenceErrorMessage,
                                       String homeOfficeCallErrorMessage) {

        switch (errorCode) {
            case 1010:
            case 1030:
                asylumCase.write(HOME_OFFICE_SEARCH_STATUS, "FAIL");
                asylumCase.write(
                        AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS_MESSAGE,
                        homeOfficeAppellantNotFoundErrorMessage);
                log.warn(
                        "Home office response returned with Appellant Not found error, caseId: {}, error message: {}",
                        caseId,
                        errMessage
                );
                break;
            case 1020:
                asylumCase.write(HOME_OFFICE_SEARCH_STATUS, "FAIL");
                asylumCase.write(
                        AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS_MESSAGE,
                        homeOfficeReferenceNotFoundErrorMessage);
                log.warn(
                        "Home office response returned with HO data Not found error, caseId: {}, error message: {}",
                        caseId,
                        errMessage
                );
                break;
            case 1060:
                asylumCase.write(HOME_OFFICE_SEARCH_STATUS, "FAIL");
                asylumCase.write(
                        AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS_MESSAGE,
                        homeOfficeInvalidReferenceErrorMessage);
                log.warn(
                        "Home office response returned with HO format error, caseId: {}, error message: {}",
                        caseId,
                        errMessage
                );
                break;
            default:
                asylumCase.write(HOME_OFFICE_SEARCH_STATUS, "FAIL");
                asylumCase.write(
                        AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS_MESSAGE, homeOfficeCallErrorMessage);
                log.warn(
                        "Home office response returned with error, caseId: {}, error message: {}",
                        caseId,
                        errMessage
                );
                break;
        }

        asylumCase.write(HOME_OFFICE_API_ERROR, INVALID_HOME_OFFICE_REFERENCE);
    }
}
