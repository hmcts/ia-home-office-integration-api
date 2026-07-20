package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.GWF_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_APPELLANTS;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_APPELLANT_CLAIM_DATE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_APPELLANT_DECISION_DATE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_APPELLANT_DECISION_LETTER_DATE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;

import java.time.LocalDate;
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
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.HomeOfficeApplicationService;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.HomeOfficeMissingApplicationException;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.RetriesExceededException;

@Slf4j
@Component
public class GetAppellantDataHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private HomeOfficeApplicationService homeOfficeApplicationService;

    public static final Pattern HOME_OFFICE_REF_PATTERN = Pattern.compile("^(([0-9]{4}\\-[0-9]{4}\\-[0-9]{4}\\-[0-9]{4})|(GWF[0-9]{9}))$");

    public GetAppellantDataHandler(HomeOfficeApplicationService homeOfficeApplicationService) {
        this.homeOfficeApplicationService = homeOfficeApplicationService;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
                && List.of(Event.START_APPEAL, Event.EDIT_APPEAL, Event.EDIT_APPEAL_AFTER_SUBMIT).contains(callback.getEvent())
                && List.of(
                    "homeOfficeReferenceNumber", "oocHomeOfficeReferenceNumber", "appellantBasicDetails", // ExUI pages
                    "cuiHomeOfficeReferenceNumber", "cuiAppellantName", "cuiAppellantDob") // CUI pages
                    .contains(callback.getPageId());
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
        // Retrieve the UAN or GWF from the case record
        String homeOfficeReferenceNumber = asylumCase
                .read(HOME_OFFICE_REFERENCE_NUMBER, String.class)
                .orElse("");
        if (homeOfficeReferenceNumber.isEmpty()) {
            homeOfficeReferenceNumber = asylumCase
                        .read(GWF_REFERENCE_NUMBER, String.class)
                        .orElseThrow(() -> new IllegalStateException(
                            "Home office reference number (UAN or GWF) is not present; caseId: " + caseId + "."));
        }

        log.info("GetAppellantDataHandler triggered for caseId: {}, event: {}, pageId: {}, reference: {}",
            caseId, callback.getEvent(), callback.getPageId(), homeOfficeReferenceNumber);

        try {
            // We want to call the Home Office /applications/{id} endpoint and write all data it returns to the case record
            ResponseEntity<HomeOfficeApplicationDto> homeOfficeResponse = homeOfficeApplicationService.getApplication(homeOfficeReferenceNumber);
            HomeOfficeApplicationDto applicationDto = homeOfficeResponse.getBody();
            log.info("GetAppellantDataHandler received response with status: {} for caseId: {}",
                homeOfficeResponse.getStatusCode().value(), caseId);
            // Error checking even though we received a 2xx status code (things could still be wrong)
            if (applicationDto == null || applicationDto.getAppellants() == null || applicationDto.getAppellants().isEmpty()) {
                throw new HomeOfficeMissingApplicationException(-2,
                            "Biographic information from Home Office asylum (etc.) application with reference " +
                             homeOfficeReferenceNumber +
                             " could not be retrieved.\n\nThe Home Office validation API responded but the response contained no data.");
            }
            log.info("GetAppellantDataHandler received {} appellant(s) from Home Office for caseId: {}",
                applicationDto.getAppellants().size(), caseId);
            // If we supplied a UAN (rather than a GWF) and the Home Office returned one, make sure they match
            if (HOME_OFFICE_REF_PATTERN.matcher(homeOfficeReferenceNumber).matches()) {
                String uan = applicationDto.getUan();
                if (uan == null) {
                    // Odd but not a show-stopper; they might not always send it back
                    log.warn("Home Office response did not contain a UAN despite the fact that the appellant is known to have one: {}.", homeOfficeReferenceNumber);
                } else if (!uan.equals(homeOfficeReferenceNumber)) {
                    // The Home Office returned a *different* UAN: very bad
                    throw new HomeOfficeMissingApplicationException(-3,
                                "Biographic information from Home Office asylum (etc.) application with reference " +
                                homeOfficeReferenceNumber +
                                " could not be retrieved.\n\nThe Home Office validation API responded but the information " +
                                "appears to be from an application with reference " + uan + ".");
                }
            }

            writeHomeOfficeDataToCase(asylumCase, homeOfficeReferenceNumber, String.valueOf(homeOfficeResponse.getStatusCode().value()), applicationDto);
            log.info("GetAppellantDataHandler successfully wrote Home Office appellant data to case: {}", caseId);

        } catch (HomeOfficeMissingApplicationException exception) {
            String message = exception.getMessage();
            // Log as an error if the return status indicates a problem somewhere in our code (which may be a result of something changing at the Home Office's end)
            switch (exception.getHttpStatus()) {
                // These negative numbers are obviously not real HTTP response codes; but they nonetheless convey useful information
                case -4, -3, -2, -1:
                    // This means we didn't get a valid response from the Home Office (badly formatted response, wrong response, empty response or time-out)
                    log.warn("GetAppellantDataHandler failed (status {}), caseId: {}: {}", exception.getHttpStatus(), caseId, message);
                    break;
                case 400, 401, 403:
                    // If the request is malformed, unauthenticated or unauthorised, it's a problem in our code
                    log.error("GetAppellantDataHandler failed (status {}), caseId: {}: {}", exception.getHttpStatus(), caseId, message);
                    break;
                case 404:
                    // This will happen regularly due to user error; the code is fine
                    log.info("GetAppellantDataHandler: UAN/GWF not found at Home Office (404), caseId: {}, reference: {}", caseId, homeOfficeReferenceNumber);
                    break;
                case 500, 501, 502, 503, 504:
                    // One of these signifies a problem at the Home Office's end - nothing we can do
                    log.warn("GetAppellantDataHandler failed (status {}), caseId: {}: {}", exception.getHttpStatus(), caseId, message);
                    break;
                default:
                    // Don't know - safest to assume it's a problem with our own code
                    log.error("GetAppellantDataHandler failed (status {}), caseId: {}: {}", exception.getHttpStatus(), caseId, message);
                    break;
            }
            // Send the HTTP status code back to the ia-case-api service by writing it in the case record
            asylumCase.write(HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS, exception.getHttpStatus());
        } catch (RetriesExceededException ex) {
            log.warn("GetAppellantDataHandler retries exhausted for caseId: {}: {}", caseId, ex.getMessage());
            asylumCase.write(HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS, -1);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void writeHomeOfficeDataToCase(
        final AsylumCase asylumCase, final String homeOfficeReferenceNumber,
        String homeOfficeResponseStatus, HomeOfficeApplicationDto applicationDto
    ) {
        asylumCase.write(HOME_OFFICE_APPELLANT_CLAIM_DATE, getDateStringSafely(applicationDto.getHoClaimDate()));
        asylumCase.write(HOME_OFFICE_APPELLANT_DECISION_DATE, getDateStringSafely(applicationDto.getHoDecisionDate()));
        asylumCase.write(HOME_OFFICE_APPELLANT_DECISION_LETTER_DATE, getDateStringSafely(applicationDto.getHoDecisionLetterDate()));

        List<IdValue<HomeOfficeAppellant>> appellants = new ArrayList<>();

        for (HomeOfficeAppellantDto appellantDto : applicationDto.getAppellants()) {
            String pp = appellantDto.getPp();
            String id = pp == null ? homeOfficeReferenceNumber : homeOfficeReferenceNumber + "/" + pp;
            try {
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
                appellants.add(new IdValue<HomeOfficeAppellant>(id, appellant));
            } catch (Exception e) {
                String message = "Biographic information from Home Office asylum (etc.) application with reference " + homeOfficeReferenceNumber
                               + " was retrieved but did not match the expected format " + (pp == null ? "" : " for appellant " + pp)
                               + ": " + e.getMessage();
                throw new HomeOfficeMissingApplicationException(-4, message);
            } 
        }

        asylumCase.write(HOME_OFFICE_APPELLANTS, appellants);
        asylumCase.write(HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS, homeOfficeResponseStatus);
    }

    private YesOrNo yesOrNoFromBoolean(Boolean value) {
        return value == null ? null : (value ? YesOrNo.YES : YesOrNo.NO);
    }

    private String getDateStringSafely(LocalDate value) {
        return value == null ? null : value.toString();
    }
}
