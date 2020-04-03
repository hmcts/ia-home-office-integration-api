package uk.gov.hmcts.reform.hois.domain.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HomeOfficeResponse {

    @JsonProperty("messageHeader")
    private MessageHeader messageHeader;

    private String messageType;

    @JsonProperty("status")
    private Status[] statuses;

    @JsonProperty("errorDetail")
    private ErrorDetail errorDetail;

    public Status[] getStatuses() {
        return Arrays.copyOf(statuses, statuses.length);
    }

    public void setStatuses(Status[] statuses) {
        this.statuses = Arrays.copyOf(statuses, statuses.length);
    }

}

