package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_APPELLANTS;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_APPELLANT_API_HTTP_STATUS;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_APPELLANT_CLAIM_DATE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_APPELLANT_DECISION_DATE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_APPELLANT_DECISION_LETTER_DATE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeAppellantDto;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.HomeOfficeApplicationDto;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.HomeOfficeAppellant;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.HomeOfficeApplicationService;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.HomeOfficeMissingApplicationException;

@Slf4j
@Component
public class GetAppellantDataHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private HomeOfficeApplicationService homeOfficeApplicationService;

    private final FeatureToggler featureToggler;

    public GetAppellantDataHandler(HomeOfficeApplicationService homeOfficeApplicationService, FeatureToggler featureToggler) {
        this.homeOfficeApplicationService = homeOfficeApplicationService;
        this.featureToggler = featureToggler;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.GET_HOME_OFFICE_APPELLANT_DATA
                && featureToggler.getValue("home-office-uan-feature", false);
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        final long caseId = callback.getCaseDetails().getId();
        final String homeOfficeReferenceNumber = asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)
            .orElseThrow(
                () -> new IllegalStateException(
                    "Home office reference number (UAN or GWF) is not present; caseId: " + caseId + "."
                )
            );

        try {
            // We want to call the Home Office /applications/{id} endpoint and write all data it returns to the case record
            HomeOfficeApplicationDto applicationDto = homeOfficeApplicationService.getApplication(homeOfficeReferenceNumber);
            asylumCase.write(HOME_OFFICE_APPELLANT_CLAIM_DATE, applicationDto.getHoClaimDate());
            asylumCase.write(HOME_OFFICE_APPELLANT_DECISION_DATE, applicationDto.getHoDecisionDate());
            asylumCase.write(HOME_OFFICE_APPELLANT_DECISION_LETTER_DATE, applicationDto.getHoDecisionLetterDate());

            for (HomeOfficeAppellantDto appellantDto : applicationDto.getAppellants()) {
                Boolean dtoRoa = appellantDto.getRoa();
                Boolean dtoAsylumSupport = appellantDto.getAsylumSupport();
                Boolean dtoHoFeeWaiver = appellantDto.getHoFeeWaiver();
                Boolean dtoInterpreterNeeded = appellantDto.getInterpreterNeeded();
                YesOrNo roa = Boolean.TRUE.equals(dtoRoa) ? YesOrNo.YES : Boolean.TRUE.equals(dtoRoa) ? YesOrNo.NO : null;
                YesOrNo asylumSupport = Boolean.TRUE.equals(dtoAsylumSupport) ? YesOrNo.YES : Boolean.TRUE.equals(dtoAsylumSupport) ? YesOrNo.NO : null;
                YesOrNo hoFeeWaiver = Boolean.TRUE.equals(dtoHoFeeWaiver) ? YesOrNo.YES : Boolean.TRUE.equals(dtoHoFeeWaiver) ? YesOrNo.NO : null;
                YesOrNo interpreterNeeded = Boolean.TRUE.equals(dtoInterpreterNeeded) ? YesOrNo.YES : Boolean.TRUE.equals(dtoInterpreterNeeded) ? YesOrNo.NO : null;
                HomeOfficeAppellant appellant = new HomeOfficeAppellant(appellantDto.getFamilyName(), 
                                                                        appellantDto.getGivenNames(), 
                                                                        appellantDto.getDateOfBirth(), 
                                                                        appellantDto.getNationality(), 
                                                                        roa, 
                                                                        asylumSupport, 
                                                                        hoFeeWaiver, 
                                                                        appellantDto.getLanguage(), 
                                                                        interpreterNeeded);
                asylumCase.write(HOME_OFFICE_APPELLANTS, appellant);
            }
            // We know the HTTP status code is 200 here (although I acknowledge this isn't great coding - but I can only get it explicitly when an exception is thrown)
            asylumCase.write(HOME_OFFICE_APPELLANT_API_HTTP_STATUS, "200");
        } catch (HomeOfficeMissingApplicationException exception) {
            // log as an error if the return status indicates a problem somewhere in our code (which may be a result of something changing at the Home Office's end)
            switch (exception.getHttpStatus()) {
                case 400:
                case 401:
                case 403:
                    // If the request is malformed, unauthenticated or unauthorised, it's a problem in our code
                    log.error(exception.getMessage());
                    break;
                case 404:
                    // This will happen regularly due to user error; the code is fine
                    log.info(exception.getMessage());
                    break;
                case 500:
                case 501:
                case 502:
                case 503:
                case 504:
                    // One of these signifies a problem at the Home Office's end - nothing we can do
                    log.warn(exception.getMessage());
                    break;
                default:
                    log.info(exception.getMessage());
                    break;
            }
            // Send the HTTP status code back to the ia-case-api service by writing it in the case record
            asylumCase.write(HOME_OFFICE_APPELLANT_API_HTTP_STATUS, exception.getHttpStatus());
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

}
