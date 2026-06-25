package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.controllers.advice;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private String errorCode;
    private String message;
    private Instant timestamp;
    private String path;
    private List<FieldError> fieldErrors;

    @Getter
    @Builder
    public static class FieldError {
        private String field;
        private String message;
        private Object rejectedValue;
    }
}
