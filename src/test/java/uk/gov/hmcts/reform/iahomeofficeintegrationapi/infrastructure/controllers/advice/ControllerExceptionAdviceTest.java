package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.controllers.advice;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.CaseGoneException;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.CaseIncompatibleException;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.CaseNotFoundException;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.HomeOfficeResponseException;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security.idam.IdentityManagerResponseException;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ControllerExceptionAdviceTest {

    @Mock
    private HttpServletRequest request;

    private ControllerExceptionAdvice handler;

    @BeforeEach
    void setUp() {
        handler = new ControllerExceptionAdvice();
        when(request.getRequestURI()).thenReturn("/test-endpoint");
    }

    @Test
    void handleCaseNotFoundException_shouldReturn404() {
        CaseNotFoundException exception = new CaseNotFoundException("Case not found for caseId: 12345");

        ResponseEntity<ErrorResponse> response = handler.handleCaseNotFoundException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("CASE_NOT_FOUND");
        assertThat(response.getBody().getMessage()).isEqualTo(
            "The 24-week status could not be set. Case not found for caseId: 12345");
        assertThat(response.getBody().getPath()).isEqualTo("/test-endpoint");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    void handleCaseGoneException_shouldReturn410() {
        CaseGoneException exception = new CaseGoneException("Case no longer exists for caseId: 12345");

        ResponseEntity<ErrorResponse> response = handler.handleCaseGoneException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.GONE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("CASE_GONE");
        assertThat(response.getBody().getMessage()).isEqualTo(
            "The 24-week status could not be set. Case no longer exists for caseId: 12345");
    }

    @Test
    void handleCaseIncompatibleException_shouldReturn422() {
        CaseIncompatibleException exception = new CaseIncompatibleException(
            "Case incompatible with supplied 24-week status", YesOrNo.YES);

        ResponseEntity<ErrorResponse> response = handler.handleCaseIncompatibleException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("CASE_INCOMPATIBLE");
        assertThat(response.getBody().getMessage()).startsWith("The 24-week status could not be set.")
            .contains("Case incompatible");
    }

    @Test
    void handleRequiredFieldMissingException_shouldReturn400() {
        RequiredFieldMissingException exception = new RequiredFieldMissingException("Field 'name' is required");

        ResponseEntity<ErrorResponse> response = handler.handleRequiredFieldMissingException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("REQUIRED_FIELD_MISSING");
        assertThat(response.getBody().getMessage()).isEqualTo(
            "The 24-week status could not be set. Field 'name' is required");
    }

    @Test
    void handleHomeOfficeResponseException_withCaseIdNotValid_shouldReturn404() {
        HomeOfficeResponseException exception = new HomeOfficeResponseException("1010", "Case ID is not valid");

        ResponseEntity<ErrorResponse> response = handler.handleHomeOfficeResponseException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("CASE_NOT_FOUND");
        assertThat(response.getBody().getMessage()).isEqualTo(
            "The 24-week status could not be set. The HMCTS appeal reference number does not correspond to any records in HMCTS systems.");
    }

    @Test
    void handleHomeOfficeResponseException_withOtherError_shouldReturn400() {
        HomeOfficeResponseException exception = new HomeOfficeResponseException("1020", "Invalid request format");

        ResponseEntity<ErrorResponse> response = handler.handleHomeOfficeResponseException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("HOME_OFFICE_ERROR");
        assertThat(response.getBody().getMessage()).isEqualTo(
            "The 24-week status could not be set. Please check the format of the HTTP headers and message body.");
    }

    @Test
    void handleIdentityManagerResponseException_shouldReturn401() {
        IdentityManagerResponseException exception = new IdentityManagerResponseException(
            "Token expired", new RuntimeException("cause"));

        ResponseEntity<ErrorResponse> response = handler.handleIdentityManagerResponseException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("AUTHENTICATION_ERROR");
        assertThat(response.getBody().getMessage()).isEqualTo("Unable to authenticate the request.");
    }

    @Test
    void handleIllegalStateException_shouldReturn409() {
        IllegalStateException exception = new IllegalStateException("Status already set for this case");

        ResponseEntity<ErrorResponse> response = handler.handleIllegalStateException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("CONFLICT");
        assertThat(response.getBody().getMessage()).isEqualTo(
            "The 24-week status could not be set. Status already set for this case");
    }

    @Test
    void handleValidationException_shouldReturn400WithFieldErrors() throws NoSuchMethodException {
        Object target = new Object();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "target");
        bindingResult.addError(new FieldError("target", "field1", "rejected", false,
            null, null, "must not be null"));
        bindingResult.addError(new FieldError("target", "field2", "invalid", false,
            null, null, "must be a valid email"));

        MethodParameter methodParameter = new MethodParameter(
            Object.class.getMethod("toString"), -1);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidationException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().getMessage()).isEqualTo(
            "The 24-week status could not be set. Please check the format of the HTTP headers and message body.");
        assertThat(response.getBody().getFieldErrors()).hasSize(2);
        assertThat(response.getBody().getFieldErrors().get(0).getField()).isEqualTo("field1");
        assertThat(response.getBody().getFieldErrors().get(0).getMessage()).isEqualTo("must not be null");
        assertThat(response.getBody().getFieldErrors().get(0).getRejectedValue()).isEqualTo("rejected");
    }

    @Test
    void handleHttpMessageNotReadableException_shouldReturn400() {
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException(
            "JSON parse error", new MockHttpInputMessage(new byte[0]));

        ResponseEntity<ErrorResponse> response = handler.handleHttpMessageNotReadableException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("MALFORMED_REQUEST");
        assertThat(response.getBody().getMessage()).isEqualTo(
            "The 24-week status could not be set. Please check the format of the HTTP headers and message body.");
    }

    @Test
    void handleGenericException_shouldReturn500WithoutExposingDetails() {
        Exception exception = new RuntimeException("Database connection failed with credentials: user=admin");

        ResponseEntity<ErrorResponse> response = handler.handleGenericException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("INTERNAL_ERROR");
        assertThat(response.getBody().getMessage()).isEqualTo(
            "The 24-week status could not be set. Please report this to HMCTS.");
        assertThat(response.getBody().getMessage()).doesNotContain("Database");
        assertThat(response.getBody().getMessage()).doesNotContain("credentials");
    }

    @Test
    void allResponses_shouldIncludeTimestampAndPath() {
        CaseNotFoundException exception = new CaseNotFoundException("Test");

        ResponseEntity<ErrorResponse> response = handler.handleCaseNotFoundException(exception, request);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTimestamp()).isNotNull();
        assertThat(response.getBody().getPath()).isEqualTo("/test-endpoint");
    }
}
