package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.roleassignment;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum RoleType {
    CASE, ORGANISATION, @JsonEnumDefaultValue UNKNOWN
}
