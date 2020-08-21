package uk.gov.hmcts.reform.iahomeofficeintegrationapi.component.testutils;

public interface CallableWithException<T> {

    T call() throws Exception;
}
