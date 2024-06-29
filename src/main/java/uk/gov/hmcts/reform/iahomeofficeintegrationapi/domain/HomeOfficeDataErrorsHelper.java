package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain;

import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_API_ERROR;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS;

import com.microsoft.applicationinsights.boot.dependencies.apachecommons.lang3.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.ErrorMessageParameters;
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

    public void setErrorMessageForErrorCode(long caseId,
                                            AsylumCase asylumCase,
                                            String errorCodeStr,
                                            String errMessage) {
        int errorCode = 0;
        try {
            errorCode = StringUtils.isNotBlank(errorCodeStr) ? Integer.parseInt(errorCodeStr) : 0;
        } catch (NumberFormatException e) {
            log.warn("Home Office error code is not a number for caseId {}, defaulting to 0", caseId);
        }
        ErrorMessageParameters parameters = new ErrorMessageParameters();
        populateErrorParameters(parameters);
        updateErrorMessage(caseId, asylumCase, errMessage, errorCode, parameters, log);
    }

    private static void populateErrorParameters(ErrorMessageParameters parameters) {
        parameters.setHomeOfficeAppellantNotFoundErrorMessage(HOME_OFFICE_APPELLANT_NOT_FOUND_ERROR_MESSAGE);
        parameters.setHomeOfficeReferenceNotFoundErrorMessage(HOME_OFFICE_REFERENCE_NOT_FOUND_ERROR_MESSAGE);
        parameters.setHomeOfficeInvalidReferenceErrorMessage(HOME_OFFICE_INVALID_REFERENCE_ERROR_MESSAGE);
        parameters.setHomeOfficeCallErrorMessage(HOME_OFFICE_CALL_ERROR_MESSAGE);
    }

    public static void updateErrorMessage(long caseId, AsylumCase asylumCase, String errMessage, int errorCode,
                                          ErrorMessageParameters errorMessageParameters, Logger log) {

        String statusMessage;
        String logMessage = switch (errorCode) {
            case 1010, 1030 -> {
                statusMessage = errorMessageParameters.getHomeOfficeAppellantNotFoundErrorMessage();
                yield "Home office response returned with Appellant Not found error";
            }
            case 1020 -> {
                statusMessage = errorMessageParameters.getHomeOfficeReferenceNotFoundErrorMessage();
                yield "Home office response returned with HO data Not found error";
            }
            case 1060 -> {
                statusMessage = errorMessageParameters.getHomeOfficeInvalidReferenceErrorMessage();
                yield "Home office response returned with HO format error";
            }
            default -> {
                statusMessage = errorMessageParameters.getHomeOfficeCallErrorMessage();
                yield "Home office response returned with error";
            }
        };

        asylumCase.write(HOME_OFFICE_SEARCH_STATUS, "FAIL");
        asylumCase.write(AsylumCaseDefinition.HOME_OFFICE_SEARCH_STATUS_MESSAGE, statusMessage);
        log.warn("{} , caseId: {}, error message: {}", logMessage, caseId, errMessage);
        asylumCase.write(HOME_OFFICE_API_ERROR, INVALID_HOME_OFFICE_REFERENCE);
    }
}
