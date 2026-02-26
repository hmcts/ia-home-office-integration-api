package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.codec.ErrorDecoder;
import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HomeOfficeMissingApplicationDecoderConfigTest {

    @Test
    void shouldCreateHomeOfficeMissingApplicationDecoderBean() throws Exception {
        HomeOfficeMissingApplicationDecoderConfig config =
            new HomeOfficeMissingApplicationDecoderConfig();

        ObjectMapper objectMapper = new ObjectMapper();

        ErrorDecoder decoder =
            config.homeOfficeMissingApplicationDecoder(objectMapper);

        assertThat(decoder).isNotNull();
        assertThat(decoder)
            .isInstanceOf(HomeOfficeMissingApplicationDecoder.class);

        // Verify that the ObjectMapper was correctly injected
        Field field = HomeOfficeMissingApplicationDecoder.class
            .getDeclaredField("objectMapper");
        field.setAccessible(true);

        Object injectedObjectMapper = field.get(decoder);

        assertThat(injectedObjectMapper).isSameAs(objectMapper);
    }
}