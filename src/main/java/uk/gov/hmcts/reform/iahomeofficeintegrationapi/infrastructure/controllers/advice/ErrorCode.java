package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.controllers.advice;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    CASE_NOT_FOUND("CASE_NOT_FOUND", HttpStatus.NOT_FOUND, null),
    CASE_GONE("CASE_GONE", HttpStatus.GONE, null),
    CASE_INCOMPATIBLE("CASE_INCOMPATIBLE", HttpStatus.UNPROCESSABLE_ENTITY, null),
    REQUIRED_FIELD_MISSING("REQUIRED_FIELD_MISSING", HttpStatus.BAD_REQUEST, null),
    CONFLICT("CONFLICT", HttpStatus.CONFLICT, null),

    HOME_OFFICE_ERROR("HOME_OFFICE_ERROR", HttpStatus.BAD_REQUEST,
        "The 24-week status could not be set. Please check the format of the HTTP headers and message body."),
    HOME_OFFICE_CASE_NOT_FOUND("CASE_NOT_FOUND", HttpStatus.NOT_FOUND,
        "The 24-week status could not be set. The HMCTS appeal reference number does not correspond to any records in HMCTS systems."),
    AUTHENTICATION_ERROR("AUTHENTICATION_ERROR", HttpStatus.UNAUTHORIZED,
        "Unable to authenticate the request."),
    VALIDATION_ERROR("VALIDATION_ERROR", HttpStatus.BAD_REQUEST,
        "The 24-week status could not be set. Please check the format of the HTTP headers and message body."),
    MALFORMED_REQUEST("MALFORMED_REQUEST", HttpStatus.BAD_REQUEST,
        "The 24-week status could not be set. Please check the format of the HTTP headers and message body."),
    INTERNAL_ERROR("INTERNAL_ERROR", HttpStatus.INTERNAL_SERVER_ERROR,
        "The 24-week status could not be set. Please report this to HMCTS.");

    private static final String ERROR_PREFIX = "The 24-week status could not be set. ";

    private final String code;
    private final HttpStatus httpStatus;
    private final String defaultMessage;

    public String formatMessage(String details) {
        return ERROR_PREFIX + details;
    }
}
