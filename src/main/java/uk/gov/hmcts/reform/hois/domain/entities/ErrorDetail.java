package uk.gov.hmcts.reform.hois.domain.entities;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@NoArgsConstructor
@Data
public class ErrorDetail {
    private String errorCode;
    private String messageText;
    private boolean success;

    public ErrorDetail(String errorCode, String messageText, boolean success) {
        this.errorCode = errorCode;
        this.messageText = messageText;
        this.success = success;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getMessageText() {
        return messageText;
    }

    public boolean isSuccess() {
        return success;
    }
}
