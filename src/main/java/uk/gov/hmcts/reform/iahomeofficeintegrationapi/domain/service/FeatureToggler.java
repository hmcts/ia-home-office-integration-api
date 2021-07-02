package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service;

public interface FeatureToggler {

    boolean getValue(String key, Boolean defaultValue);

}
