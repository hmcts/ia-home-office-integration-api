package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security;

public interface SystemUserProvider {

    String getSystemUserId(String userToken);
}
