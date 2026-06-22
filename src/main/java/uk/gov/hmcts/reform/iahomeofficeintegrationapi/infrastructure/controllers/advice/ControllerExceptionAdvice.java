package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.controllers.advice;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.CaseGoneException;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.CaseIncompatibleException;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.CaseNotFoundException;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.HomeOfficeResponseException;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security.idam.IdentityManagerResponseException;

@Slf4j
@ControllerAdvice(basePackages = "uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.controllers")
@RequestMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
public class ControllerExceptionAdvice {

    @ExceptionHandler(CaseNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCaseNotFoundException(
            CaseNotFoundException ex, HttpServletRequest request) {
        log.info("Case not found: {}", ex.getMessage());
        return buildResponse(ErrorCode.CASE_NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(CaseGoneException.class)
    public ResponseEntity<ErrorResponse> handleCaseGoneException(
            CaseGoneException ex, HttpServletRequest request) {
        log.info("Case gone: {}", ex.getMessage());
        return buildResponse(ErrorCode.CASE_GONE, ex.getMessage(), request);
    }

    @ExceptionHandler(CaseIncompatibleException.class)
    public ResponseEntity<ErrorResponse> handleCaseIncompatibleException(
            CaseIncompatibleException ex, HttpServletRequest request) {
        log.info("Case incompatible: {}", ex.getMessage());
        return buildResponse(ErrorCode.CASE_INCOMPATIBLE, ex.getMessage(), request);
    }

    @ExceptionHandler(RequiredFieldMissingException.class)
    public ResponseEntity<ErrorResponse> handleRequiredFieldMissingException(
            RequiredFieldMissingException ex, HttpServletRequest request) {
        log.info("Required field missing: {}", ex.getMessage());
        return buildResponse(ErrorCode.REQUIRED_FIELD_MISSING, ex.getMessage(), request);
    }

    @ExceptionHandler(HomeOfficeResponseException.class)
    public ResponseEntity<ErrorResponse> handleHomeOfficeResponseException(
            HomeOfficeResponseException ex, HttpServletRequest request) {
        String message = ex.getMessage();

        if (message != null && message.contains("Case ID is not valid")) {
            log.info("Case not found: {}", message);
            return buildResponse(ErrorCode.HOME_OFFICE_CASE_NOT_FOUND, request);
        }

        log.info("Home Office response error: {}", message);
        return buildResponse(ErrorCode.HOME_OFFICE_ERROR, request);
    }

    @ExceptionHandler(IdentityManagerResponseException.class)
    public ResponseEntity<ErrorResponse> handleIdentityManagerResponseException(
            IdentityManagerResponseException ex, HttpServletRequest request) {
        log.error("Identity manager error: {}", ex.getMessage());
        return buildResponse(ErrorCode.AUTHENTICATION_ERROR, request);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(
            IllegalStateException ex, HttpServletRequest request) {
        log.info("Conflict error: {}", ex.getMessage());
        return buildResponse(ErrorCode.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> ErrorResponse.FieldError.builder()
                .field(error.getField())
                .message(error.getDefaultMessage())
                .rejectedValue(error.getRejectedValue())
                .build())
            .toList();

        log.info("Validation error: {} field error(s)", fieldErrors.size());

        ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;
        ErrorResponse response = ErrorResponse.builder()
            .errorCode(errorCode.getCode())
            .message(errorCode.getDefaultMessage())
            .timestamp(Instant.now())
            .path(request.getRequestURI())
            .fieldErrors(fieldErrors)
            .build();

        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.info("Unreadable HTTP message error: {}", ex.getMessage());
        return buildResponse(ErrorCode.MALFORMED_REQUEST, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        log.error("Unspecified server error: [path={}]: {}",
            request.getRequestURI(), ex.getMessage(), ex);
        return buildResponse(ErrorCode.INTERNAL_ERROR, request);
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            ErrorCode errorCode, String exceptionMessage, HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.builder()
            .errorCode(errorCode.getCode())
            .message(errorCode.formatMessage(exceptionMessage))
            .timestamp(Instant.now())
            .path(request.getRequestURI())
            .build();
        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            ErrorCode errorCode, HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.builder()
            .errorCode(errorCode.getCode())
            .message(errorCode.getDefaultMessage())
            .timestamp(Instant.now())
            .path(request.getRequestURI())
            .build();
        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }
}
