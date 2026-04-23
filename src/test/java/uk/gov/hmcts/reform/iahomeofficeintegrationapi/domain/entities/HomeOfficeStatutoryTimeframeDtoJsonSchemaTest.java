package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

class HomeOfficeStatutoryTimeframeDtoJsonSchemaTest {

    private ObjectMapper objectMapper;
    private JsonSchema schema;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String jsonSchemaFile = "twentyFourWeekStatusSchema_v8.json";

        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);
        InputStream schemaStream = getClass().getResourceAsStream("/" + jsonSchemaFile);
        
        if (schemaStream == null) {
            fail("Schema file " + jsonSchemaFile + " not found in test resources");
        }
        
        schema = factory.getSchema(schemaStream);
    }

    @Test
    void shouldValidateAgainstJsonSchemaWithAcceleratedAppealTrue() throws Exception {
        // Given
        HomeOfficeStatutoryTimeframeDto.Stf24WeekCohort cohort = 
            HomeOfficeStatutoryTimeframeDto.Stf24WeekCohort.builder()
                .name("HU")
                .included("true")
                .build();
        HomeOfficeStatutoryTimeframeDto dto = HomeOfficeStatutoryTimeframeDto.builder()
            .hmctsReferenceNumber("PA/12345/2026")
            .uan("1234-5678-9012-3456")
            .familyName("Smith")
            .givenNames("John")
            .dateOfBirth(LocalDate.of(1990, 5, 15))
            .stf24weekCohorts(List.of(cohort))
            .timeStamp(OffsetDateTime.of(2023, 12, 1, 14, 30, 45, 0, ZoneOffset.UTC))
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
        HomeOfficeStatutoryTimeframeDto.Stf24WeekCohort cohort = 
            HomeOfficeStatutoryTimeframeDto.Stf24WeekCohort.builder()
                .name("HU")
                .included("true")
                .build();
        HomeOfficeStatutoryTimeframeDto dto = HomeOfficeStatutoryTimeframeDto.builder()
            .hmctsReferenceNumber("HU/54321/2021")
            .uan("9876-5432-1098-7654")
            .familyName("Doe")
            .givenNames("Jane")
            .dateOfBirth(LocalDate.of(1985, 3, 20))
            .stf24weekCohorts(List.of(cohort))
            .timeStamp(OffsetDateTime.of(2024, 1, 15, 10, 20, 30, 0, ZoneOffset.UTC))
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
        HomeOfficeStatutoryTimeframeDto.Stf24WeekCohort cohort = 
            HomeOfficeStatutoryTimeframeDto.Stf24WeekCohort.builder()
                .name("HU")
                .included("true")
                .build();
        HomeOfficeStatutoryTimeframeDto dto = HomeOfficeStatutoryTimeframeDto.builder()
            .hmctsReferenceNumber("RP/23432/2006")
            .uan("1111-2222-3333-4444")
            .familyName("Test")
            .givenNames("User")
            .dateOfBirth(LocalDate.of(2000, 12, 31))
            .stf24weekCohorts(List.of(cohort))
            .timeStamp(OffsetDateTime.of(2024, 6, 15, 23, 59, 59, 0, ZoneOffset.UTC))
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
        HomeOfficeStatutoryTimeframeDto.Stf24WeekCohort cohort = 
            HomeOfficeStatutoryTimeframeDto.Stf24WeekCohort.builder()
                .name("HU")
                .included("true")
                .build();
        HomeOfficeStatutoryTimeframeDto dto = HomeOfficeStatutoryTimeframeDto.builder()
            .hmctsReferenceNumber("EA/98765/1976")
            .uan("5555-6666-7777-8888")
            .familyName("Midnight")
            .givenNames("Test")
            .dateOfBirth(LocalDate.of(1995, 6, 10))
            .stf24weekCohorts(List.of(cohort))
            .timeStamp(OffsetDateTime.of(2023, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC))
            .build();

        // When
        String json = objectMapper.writeValueAsString(dto);
        JsonNode jsonNode = objectMapper.readTree(json);
        Set<ValidationMessage> errors = schema.validate(jsonNode);

        // Then
        assertTrue(errors.isEmpty(), "JSON should validate against schema with midnight time. Errors: " + errors);
    }

    @Test
    void shouldValidateAgainstJsonSchemaWithMissingUan() throws Exception {
        // Given
        HomeOfficeStatutoryTimeframeDto.Stf24WeekCohort cohort = 
            HomeOfficeStatutoryTimeframeDto.Stf24WeekCohort.builder()
                .name("HU")
                .included("true")
                .build();
        HomeOfficeStatutoryTimeframeDto dto = HomeOfficeStatutoryTimeframeDto.builder()
            .hmctsReferenceNumber("EU/66666/1842")
            .familyName("Doe")
            .givenNames("Jane")
            .dateOfBirth(LocalDate.of(1985, 3, 20))
            .stf24weekCohorts(List.of(cohort))
            .timeStamp(OffsetDateTime.of(2024, 1, 15, 10, 20, 30, 0, ZoneOffset.UTC))
            .build();

        // When
        String json = objectMapper.writeValueAsString(dto);
        JsonNode jsonNode = objectMapper.readTree(json);
        Set<ValidationMessage> errors = schema.validate(jsonNode);

        // Then
        assertTrue(errors.isEmpty(), "JSON should validate against schema. Errors: " + errors);
    }

    @Test
    void shouldValidateAgainstJsonSchemaWithNullStatus() throws Exception {
        // Given
        HomeOfficeStatutoryTimeframeDto.Stf24WeekCohort cohort = 
            HomeOfficeStatutoryTimeframeDto.Stf24WeekCohort.builder()
                .name("HU")
                .included("true")
                .build();
        HomeOfficeStatutoryTimeframeDto dto = HomeOfficeStatutoryTimeframeDto.builder()
            .hmctsReferenceNumber("EU/66666/1842")
            .uan("1122-3344-5566-7788")
            .familyName("Doe")
            .givenNames("Jane")
            .dateOfBirth(LocalDate.of(1985, 3, 20))
            .stf24weekCohorts(List.of(cohort))
            .timeStamp(OffsetDateTime.of(2024, 1, 15, 10, 20, 30, 0, ZoneOffset.UTC))
            .build();

        // When
        String json = objectMapper.writeValueAsString(dto);
        JsonNode jsonNode = objectMapper.readTree(json);
        Set<ValidationMessage> errors = schema.validate(jsonNode);

        // Then
        assertTrue(errors.isEmpty(), "JSON should validate against schema. Errors: " + errors);
    }

    @Test
    void shouldFailValidationWhenUanHasInvalidFormat() throws Exception {
        // Given - UAN without dashes
        HomeOfficeStatutoryTimeframeDto.Stf24WeekCohort cohort = 
            HomeOfficeStatutoryTimeframeDto.Stf24WeekCohort.builder()
                .name("HU")
                .included("true")
                .build();
        HomeOfficeStatutoryTimeframeDto dto = HomeOfficeStatutoryTimeframeDto.builder()
            .hmctsReferenceNumber("PA/12345/2026")
            .uan("1234567890123456")
            .familyName("Test")
            .givenNames("User")
            .dateOfBirth(LocalDate.of(1995, 6, 10))
            .stf24weekCohorts(List.of(cohort))
            .timeStamp(OffsetDateTime.of(2023, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC))
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
    void shouldPassValidationWithEmptyCohortArray() throws Exception {
        // Given

        HomeOfficeStatutoryTimeframeDto dto = HomeOfficeStatutoryTimeframeDto.builder()
            .hmctsReferenceNumber("EU/66666/1842")
            .uan("1122-3344-5566-7788")
            .familyName("Doe")
            .givenNames("Jane")
            .dateOfBirth(LocalDate.of(1985, 3, 20))
            .stf24weekCohorts(List.of())
            .timeStamp(OffsetDateTime.of(2024, 1, 15, 10, 20, 30, 0, ZoneOffset.UTC))
            .build();

        // When
        String json = objectMapper.writeValueAsString(dto);
        JsonNode jsonNode = objectMapper.readTree(json);
        Set<ValidationMessage> errors = schema.validate(jsonNode);

        // Then
        assertTrue(errors.isEmpty(), "JSON should pass validation with empty cohort array");
    }

    @Test
    void shouldFailValidationWhenUanContainsLetters() throws Exception {
        // Given - UAN with letters
        HomeOfficeStatutoryTimeframeDto.Stf24WeekCohort cohort = 
            HomeOfficeStatutoryTimeframeDto.Stf24WeekCohort.builder()
                .name("HU")
                .included("true")
                .build();
        HomeOfficeStatutoryTimeframeDto dto = HomeOfficeStatutoryTimeframeDto.builder()
            .hmctsReferenceNumber("PA/12345/2026")
            .uan("ABCD-5678-9012-3456")
            .familyName("Test")
            .givenNames("User")
            .dateOfBirth(LocalDate.of(1995, 6, 10))
            .stf24weekCohorts(List.of(cohort))
            .timeStamp(OffsetDateTime.of(2023, 1, 1, 12, 30, 45, 0, ZoneOffset.UTC))
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
        HomeOfficeStatutoryTimeframeDto.Stf24WeekCohort cohort = 
            HomeOfficeStatutoryTimeframeDto.Stf24WeekCohort.builder()
                .name("HU")
                .included("true")
                .build();
        HomeOfficeStatutoryTimeframeDto dto = HomeOfficeStatutoryTimeframeDto.builder()
            .hmctsReferenceNumber("PA/12345/2026")
            .uan("123-456-789-012")
            .familyName("Test")
            .givenNames("User")
            .dateOfBirth(LocalDate.of(1995, 6, 10))
            .stf24weekCohorts(List.of(cohort))
            .timeStamp(OffsetDateTime.of(2023, 1, 1, 12, 30, 45, 0, ZoneOffset.UTC))
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

    @Test
    void shouldFailValidationWhenHmctsRefNumIsWrong() throws Exception {
        // Given - wrong HMCTS reference number
        HomeOfficeStatutoryTimeframeDto.Stf24WeekCohort cohort = 
            HomeOfficeStatutoryTimeframeDto.Stf24WeekCohort.builder()
                .name("HU")
                .included("true")
                .build();

        HomeOfficeStatutoryTimeframeDto dto = HomeOfficeStatutoryTimeframeDto.builder()
            .hmctsReferenceNumber("1234567890123456")
            .uan("1233-4456-7899-0012")
            .familyName("Test")
            .givenNames("User")
            .dateOfBirth(LocalDate.of(1995, 6, 10))
            .stf24weekCohorts(List.of(cohort))
            .timeStamp(OffsetDateTime.of(2023, 1, 1, 12, 30, 45, 0, ZoneOffset.UTC))
            .build();

        // When
        String json = objectMapper.writeValueAsString(dto);
        JsonNode jsonNode = objectMapper.readTree(json);
        Set<ValidationMessage> errors = schema.validate(jsonNode);

        // Then
        assertTrue(!errors.isEmpty(), "JSON should fail validation with wrong HMCTS reference number");
        assertTrue(errors.stream().anyMatch(e -> e.getMessage().contains("hmctsReferenceNumber")), 
                   "Error should be related to HMCTS reference number field. Errors: " + errors);
    }

    @Test
    void shouldFailValidationWhenHmctsRefNumIsMissing() throws Exception {
        // Given - wrong HMCTS reference number
        HomeOfficeStatutoryTimeframeDto.Stf24WeekCohort cohort = 
            HomeOfficeStatutoryTimeframeDto.Stf24WeekCohort.builder()
                .name("HU")
                .included("true")
                .build();
        HomeOfficeStatutoryTimeframeDto dto = HomeOfficeStatutoryTimeframeDto.builder()
            .uan("1233-4456-7899-0012")
            .familyName("Test")
            .givenNames("User")
            .dateOfBirth(LocalDate.of(1995, 6, 10))
            .stf24weekCohorts(List.of(cohort))
            .timeStamp(OffsetDateTime.of(2023, 1, 1, 12, 30, 45, 0, ZoneOffset.UTC))
            .build();

        // When
        String json = objectMapper.writeValueAsString(dto);
        JsonNode jsonNode = objectMapper.readTree(json);
        Set<ValidationMessage> errors = schema.validate(jsonNode);

        // Then
        assertTrue(!errors.isEmpty(), "JSON should fail validation with wrong HMCTS reference number");
        assertTrue(errors.stream().anyMatch(e -> e.getMessage().contains("hmctsReferenceNumber")), 
                   "Error should be related to HMCTS reference number field. Errors: " + errors);
    }
}
