package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.codec.ErrorDecoder;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class FeignErrorDecoderConfigTest {

    private final FeignErrorDecoderConfig config = new FeignErrorDecoderConfig();

    @Test
    void shouldCreateHomeOfficeErrorDecoderBean() {
        // Arrange
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);

        // Act
        ErrorDecoder errorDecoder = config.homeOfficeErrorDecoder(objectMapper);

        // Assert
        assertThat(errorDecoder).isNotNull();
        assertThat(errorDecoder).isInstanceOf(FeignErrorDecoder.class);
    }

    @Test
    void shouldUseProvidedObjectMapperInstance() throws Exception {
        // Arrange
        ObjectMapper objectMapper = new ObjectMapper();

        // Act
        FeignErrorDecoder decoder =
            (FeignErrorDecoder) config.homeOfficeErrorDecoder(objectMapper);

        // Use reflection to verify the ObjectMapper was injected correctly
        var field = FeignErrorDecoder.class.getDeclaredField("objectMapper");
        field.setAccessible(true);
        Object injectedMapper = field.get(decoder);

        // Assert
        assertThat(injectedMapper).isSameAs(objectMapper);
    }
}