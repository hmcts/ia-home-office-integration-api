package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.serialization;

public interface Serializer<T> {

    String serialize(T data);
}
