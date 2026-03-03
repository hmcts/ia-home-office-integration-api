package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_APPELLANTS;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_APPELLANT_API_HTTP_STATUS;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_APPELLANT_CLAIM_DATE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_APPELLANT_DECISION_DATE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_APPELLANT_DECISION_LETTER_DATE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.http.ResponseEntity;
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
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.HomeOfficeApplicationService;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.HomeOfficeMissingApplicationException;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.RetriesExceededException;

@Slf4j
@Component
public class GetAppellantDataHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private HomeOfficeApplicationService homeOfficeApplicationService;
    private final FeatureToggler featureToggler;

    public static final Pattern HOME_OFFICE_REF_PATTERN = Pattern.compile("^(([0-9]{4}\\-[0-9]{4}\\-[0-9]{4}\\-[0-9]{4})|(GWF[0-9]{9}))$");

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

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
                && featureToggler.getValue("home-office-uan-feature", false)
                && (callback.getEvent() == Event.START_APPEAL
                || callback.getEvent() == Event.EDIT_APPEAL)
                // && (callback.getPageId().equals("homeOfficeReferenceNumber_TEMPORARILY_DISABLED") || 
                //     // TODO - add logic for this case below (the other two have been implemented, whereas this one hasn't)
                //     callback.getPageId().equals("oocHomeOfficeReferenceNumber_TEMPORARILY_DISABLED") ||
                //     callback.getPageId().equals("appellantBasicDetails_TEMPORARILY_DISABLED"));
                && (callback.getPageId().equals("homeOfficeReferenceNumber") || 
                    // TODO - add logic for this case below (the other two have been implemented, whereas this one hasn't)
                    callback.getPageId().equals("oocHomeOfficeReferenceNumber") ||
                    callback.getPageId().equals("appellantBasicDetails"));
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
            ResponseEntity<HomeOfficeApplicationDto> homeOfficeResponse = homeOfficeApplicationService.getApplication(homeOfficeReferenceNumber);
            HomeOfficeApplicationDto applicationDto = homeOfficeResponse.getBody();
            // Error checking even though we received a 2xx status code (things could still be wrong)
            if (applicationDto == null || applicationDto.getAppellants() == null || applicationDto.getAppellants().isEmpty()) {
                throw new HomeOfficeMissingApplicationException(-2, 
                            "Biographic information from Home Office asylum (etc.) application with HMCTS reference " +
                             homeOfficeReferenceNumber +
                             " could not be retrieved.\n\nThe Home Office validation API responded but the necessary information was not present.");
            }
            // If we supplied a UAN (rather than a GWF) and the Home Office returned one, make sure they match 
            if (HOME_OFFICE_REF_PATTERN.matcher(homeOfficeReferenceNumber).matches()) {
                String uan = applicationDto.getUan();
                if (uan == null) {
                    // Odd but not a show-stopper; they might not always send it back
                    log.warn("Home Office response did not contain a UAN despite the fact that the appellant is known to have one: {}.", homeOfficeReferenceNumber);
                } else if (!uan.equals(homeOfficeReferenceNumber)) {
                    // The Home Office returned a *different* UAN: very bad
                    throw new HomeOfficeMissingApplicationException(-3, 
                                "Biographic information from Home Office asylum (etc.) application with HMCTS reference " +
                                homeOfficeReferenceNumber +
                                " could not be retrieved.\n\nThe Home Office validation API responded but the information " + 
                                "appears to be from an application with reference " + uan + ".");                    
                }
            }

            asylumCase.write(HOME_OFFICE_APPELLANT_CLAIM_DATE, applicationDto.getHoClaimDate());
            asylumCase.write(HOME_OFFICE_APPELLANT_DECISION_DATE, applicationDto.getHoDecisionDate());
            asylumCase.write(HOME_OFFICE_APPELLANT_DECISION_LETTER_DATE, applicationDto.getHoDecisionLetterDate());

            List<IdValue<HomeOfficeAppellant>> appellants = new ArrayList<>();
            for (HomeOfficeAppellantDto appellantDto : applicationDto.getAppellants()) {
                String pp = appellantDto.getPp();
                HomeOfficeAppellant appellant = new HomeOfficeAppellant(pp,
                                                                        appellantDto.getFamilyName(), 
                                                                        appellantDto.getGivenNames(), 
                                                                        appellantDto.getDateOfBirth().toString(), 
                                                                        appellantDto.getNationality(), 
                                                                        yesOrNoFromBoolean(appellantDto.getRoa()), 
                                                                        yesOrNoFromBoolean(appellantDto.getAsylumSupport()), 
                                                                        yesOrNoFromBoolean(appellantDto.getHoFeeWaiver()), 
                                                                        appellantDto.getLanguage(), 
                                                                        yesOrNoFromBoolean(appellantDto.getInterpreterNeeded()));
                String id = pp == null ? homeOfficeReferenceNumber : homeOfficeReferenceNumber + "/" + pp; 
                appellants.add(new IdValue<HomeOfficeAppellant>(id, appellant));
            }
            asylumCase.write(HOME_OFFICE_APPELLANTS, appellants);
            asylumCase.write(HOME_OFFICE_APPELLANT_API_HTTP_STATUS, String.valueOf(homeOfficeResponse.getStatusCodeValue()));
        } catch (HomeOfficeMissingApplicationException exception) {
            String message = exception.getMessage();
            // Log as an error if the return status indicates a problem somewhere in our code (which may be a result of something changing at the Home Office's end)
            switch (exception.getHttpStatus()) {
                // These negative numbers are obviously not real HTTP response codes; but they nonetheless convey useful information
                case -3, -2, -1:
                    // This means we didn't get a valid response from the Home Office (wrong response, empty response or time-out)
                    log.warn(message);
                    break;
                case 400, 401, 403:
                    // If the request is malformed, unauthenticated or unauthorised, it's a problem in our code
                    log.error(message);
                    break;
                case 404:
                    // This will happen regularly due to user error; the code is fine
                    log.info(message);
                    break;
                case 500, 501, 502, 503, 504:
                    // One of these signifies a problem at the Home Office's end - nothing we can do
                    log.warn(message);
                    break;
                default:
                    // Don't know - safest to assume it's a problem with our own code
                    log.error(message);
                    break;
            }
            // Send the HTTP status code back to the ia-case-api service by writing it in the case record
            asylumCase.write(HOME_OFFICE_APPELLANT_API_HTTP_STATUS, exception.getHttpStatus());
        } catch (RetriesExceededException ex) {
            log.warn("Retries exhausted calling Home Office", ex);
            asylumCase.write(HOME_OFFICE_APPELLANT_API_HTTP_STATUS, -1);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private YesOrNo yesOrNoFromBoolean(Boolean value) {
        return value == null ? null : (value ? YesOrNo.YES : YesOrNo.NO);
    }
}
