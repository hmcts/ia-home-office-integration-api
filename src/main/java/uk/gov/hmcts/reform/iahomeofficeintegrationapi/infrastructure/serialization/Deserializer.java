package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.serialization;

public interface Deserializer<T> {

    T deserialize(String source);
}
