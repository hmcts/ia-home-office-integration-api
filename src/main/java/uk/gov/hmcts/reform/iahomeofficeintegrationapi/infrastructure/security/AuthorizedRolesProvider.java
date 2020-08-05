package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security;

import java.util.Set;

public interface AuthorizedRolesProvider {

    Set<String> getRoles();

}
