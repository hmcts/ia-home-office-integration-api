package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.roleassignment;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.Builder;

@Builder
@JsonInclude(Include.NON_NULL)
public final class QueryRequest {

    private final List<String> actorId;
    private final List<RoleType> roleType;
    private final List<RoleName> roleName;
    private final List<Classification> classification;
    private final List<GrantType> grantType;
    private final LocalDateTime validAt;
    private final List<RoleCategory> roleCategory;
    private final Map<String, List<String>> attributes;
    private final List<String> authorisations;
}
