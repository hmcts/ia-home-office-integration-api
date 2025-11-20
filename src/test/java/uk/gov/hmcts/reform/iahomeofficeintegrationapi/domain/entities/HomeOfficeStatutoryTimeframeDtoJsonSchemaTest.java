package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class HomeOfficeStatutoryTimeframeDtoJsonSchemaTest {

    private ObjectMapper objectMapper;
    private JsonSchema schema;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);
        InputStream schemaStream = getClass().getResourceAsStream("/twentyFourWeekStatusSchema_v2.json");
        
        if (schemaStream == null) {
            fail("Schema file twentyFourWeekStatusSchema_v2.json not found in test resources");
        }
        
        schema = factory.getSchema(schemaStream);
    }

    @Test
    void shouldValidateAgainstJsonSchemaWithAcceleratedAppealTrue() throws Exception {
        // Given
        HomeOfficeStatutoryTimeframeDto dto = HomeOfficeStatutoryTimeframeDto.builder()
            .uan("1234-5678-9012-3456")
            .familyName("Smith")
            .givenNames("John")
            .dateOfBirth(LocalDate.of(1990, 5, 15))
            .hoStatutoryTimeframeStatus(true)
            .timeStamp(LocalDateTime.of(2023, 12, 1, 14, 30, 45))
            .build();

        // When
        String json = objectMapper.writeValueAsString(dto);
        JsonNode jsonNode = objectMapper.readTree(json);
        Set<ValidationMessage> errors = schema.validate(jsonNode);

        // Then
        assertTrue(errors.isEmpty(), "JSON should validate against schema. Errors: " + errors);
    }

    @Test
    void shouldValidateAgainstJsonSchemaWithAcceleratedAppealFalse() throws Exception {
        // Given
        HomeOfficeStatutoryTimeframeDto dto = HomeOfficeStatutoryTimeframeDto.builder()
            .uan("9876-5432-1098-7654")
            .familyName("Doe")
            .givenNames("Jane")
            .dateOfBirth(LocalDate.of(1985, 3, 20))
            .hoStatutoryTimeframeStatus(false)
            .timeStamp(LocalDateTime.of(2024, 1, 15, 10, 20, 30))
            .build();

        // When
        String json = objectMapper.writeValueAsString(dto);
        JsonNode jsonNode = objectMapper.readTree(json);
        Set<ValidationMessage> errors = schema.validate(jsonNode);

        // Then
        assertTrue(errors.isEmpty(), "JSON should validate against schema. Errors: " + errors);
    }

    @Test
    void shouldValidateTimeStampFormatWithEdgeCaseTime() throws Exception {
        // Given - test with edge case time values
        HomeOfficeStatutoryTimeframeDto dto = HomeOfficeStatutoryTimeframeDto.builder()
            .uan("1111-2222-3333-4444")
            .familyName("Test")
            .givenNames("User")
            .dateOfBirth(LocalDate.of(2000, 12, 31))
            .hoStatutoryTimeframeStatus(true)
            .timeStamp(LocalDateTime.of(2024, 6, 15, 23, 59, 59))
            .build();

        // When
        String json = objectMapper.writeValueAsString(dto);
        JsonNode jsonNode = objectMapper.readTree(json);
        Set<ValidationMessage> errors = schema.validate(jsonNode);

        // Then
        assertTrue(errors.isEmpty(), "JSON should validate against schema with edge case time. Errors: " + errors);
    }

    @Test
    void shouldValidateTimeStampFormatWithMidnightTime() throws Exception {
        // Given
        HomeOfficeStatutoryTimeframeDto dto = HomeOfficeStatutoryTimeframeDto.builder()
            .uan("5555-6666-7777-8888")
            .familyName("Midnight")
            .givenNames("Test")
            .dateOfBirth(LocalDate.of(1995, 6, 10))
            .hoStatutoryTimeframeStatus(false)
            .timeStamp(LocalDateTime.of(2023, 1, 1, 0, 0, 0))
            .build();

        // When
        String json = objectMapper.writeValueAsString(dto);
        JsonNode jsonNode = objectMapper.readTree(json);
        Set<ValidationMessage> errors = schema.validate(jsonNode);

        // Then
        assertTrue(errors.isEmpty(), "JSON should validate against schema with midnight time. Errors: " + errors);
    }
    @Test
    void shouldFailValidationWhenUanHasInvalidFormat() throws Exception {
        // Given - UAN without dashes
        HomeOfficeStatutoryTimeframeDto dto = HomeOfficeStatutoryTimeframeDto.builder()
            .uan("1234567890123456")
            .familyName("Test")
            .givenNames("User")
            .dateOfBirth(LocalDate.of(1995, 6, 10))
            .hoStatutoryTimeframeStatus(false)
            .timeStamp(LocalDateTime.of(2023, 1, 1, 0, 0, 0))
            .build();

        // When
        String json = objectMapper.writeValueAsString(dto);
        JsonNode jsonNode = objectMapper.readTree(json);
        Set<ValidationMessage> errors = schema.validate(jsonNode);

        // Then
        assertTrue(!errors.isEmpty(), "JSON should fail validation with invalid UAN format");
        assertTrue(errors.stream().anyMatch(e -> e.getMessage().contains("uan")), 
                   "Error should be related to UAN field. Errors: " + errors);
    }

    @Test
    void shouldFailValidationWhenUanContainsLetters() throws Exception {
        // Given - UAN with letters
        HomeOfficeStatutoryTimeframeDto dto = HomeOfficeStatutoryTimeframeDto.builder()
            .uan("ABCD-5678-9012-3456")
            .familyName("Test")
            .givenNames("User")
            .dateOfBirth(LocalDate.of(1995, 6, 10))
            .hoStatutoryTimeframeStatus(true)
            .timeStamp(LocalDateTime.of(2023, 1, 1, 12, 30, 45))
            .build();

        // When
        String json = objectMapper.writeValueAsString(dto);
        JsonNode jsonNode = objectMapper.readTree(json);
        Set<ValidationMessage> errors = schema.validate(jsonNode);

        // Then
        assertTrue(!errors.isEmpty(), "JSON should fail validation with letters in UAN");
        assertTrue(errors.stream().anyMatch(e -> e.getMessage().contains("uan")), 
                   "Error should be related to UAN field. Errors: " + errors);
    }

    @Test
    void shouldFailValidationWhenUanIsTooShort() throws Exception {
        // Given - UAN with wrong length
        HomeOfficeStatutoryTimeframeDto dto = HomeOfficeStatutoryTimeframeDto.builder()
            .uan("123-456-789-012")
            .familyName("Test")
            .givenNames("User")
            .dateOfBirth(LocalDate.of(1995, 6, 10))
            .hoStatutoryTimeframeStatus(true)
            .timeStamp(LocalDateTime.of(2023, 1, 1, 12, 30, 45))
            .build();

        // When
        String json = objectMapper.writeValueAsString(dto);
        JsonNode jsonNode = objectMapper.readTree(json);
        Set<ValidationMessage> errors = schema.validate(jsonNode);

        // Then
        assertTrue(!errors.isEmpty(), "JSON should fail validation with short UAN");
        assertTrue(errors.stream().anyMatch(e -> e.getMessage().contains("uan")), 
                   "Error should be related to UAN field. Errors: " + errors);
    }
}
