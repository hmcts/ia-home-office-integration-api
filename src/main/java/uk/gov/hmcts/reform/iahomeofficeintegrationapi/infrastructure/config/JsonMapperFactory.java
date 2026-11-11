package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.databind.json.JsonMapper;

import static tools.jackson.databind.DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES;
import static tools.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static tools.jackson.databind.cfg.EnumFeature.READ_ENUMS_USING_TO_STRING;
import static tools.jackson.databind.cfg.EnumFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE;
import static tools.jackson.databind.cfg.EnumFeature.WRITE_ENUMS_USING_TO_STRING;

public final class JsonMapperFactory {

    public static final JsonMapper CUSTOM_MAPPER = create();

    private JsonMapperFactory() {
    }

    public static JsonMapper create() {
        return JsonMapper.builder()
                .enable(
                        READ_ENUMS_USING_TO_STRING,
                        READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE,
                        WRITE_ENUMS_USING_TO_STRING)
                .disable(FAIL_ON_UNKNOWN_PROPERTIES, FAIL_ON_NULL_FOR_PRIMITIVES)
                .changeDefaultPropertyInclusion(incl -> incl
                        .withValueInclusion(JsonInclude.Include.NON_ABSENT)
                        .withContentInclusion(JsonInclude.Include.NON_ABSENT))
                .build();
    }
}
