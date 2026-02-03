package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HomeOfficeStatutoryTimeframeDtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPassValidationWithValidData() {
        // Given
        HomeOfficeStatutoryTimeframeDto dto = HomeOfficeStatutoryTimeframeDto.builder()
            .ccdCaseId("1234567890123456")
            .uan("1234-5678-9012-3456")
            .familyName("Smith")
            .givenNames("John")
            .dateOfBirth(LocalDate.of(1990, 1, 1))
            .stf24weeks(HomeOfficeStatutoryTimeframeDto.Stf24Weeks.builder()
                .status("Yes")
                .cohorts(new String[]{"HU"})
                .build())
            .timeStamp(LocalDateTime.of(2023, 12, 1, 14, 30, 0))
            .build();

        // When
        Set<ConstraintViolation<HomeOfficeStatutoryTimeframeDto>> violations = validator.validate(dto);

        // Then
        assertTrue(violations.isEmpty(), "Should have no violations");
    }

    @Test
    void shouldFailValidationWhenCcdCaseIdIsTooShort() {
        // Given
        HomeOfficeStatutoryTimeframeDto dto = HomeOfficeStatutoryTimeframeDto.builder()
            .ccdCaseId("12345")
            .uan("1234-5678-9012-3456")
            .familyName("Smith")
            .givenNames("John")
            .dateOfBirth(LocalDate.of(1990, 1, 1))
            .stf24weeks(HomeOfficeStatutoryTimeframeDto.Stf24Weeks.builder()
                .status("Yes")
                .cohorts(new String[]{"HU"})
                .build())
            .timeStamp(LocalDateTime.of(2023, 12, 1, 14, 30, 0))
            .build();

        // When
        Set<ConstraintViolation<HomeOfficeStatutoryTimeframeDto>> violations = validator.validate(dto);

        // Then
        assertEquals(1, violations.size());
        ConstraintViolation<HomeOfficeStatutoryTimeframeDto> violation = violations.iterator().next();
        assertEquals("ccdCaseId", violation.getPropertyPath().toString());
        assertEquals("CCD Case ID must be a 16-digit number", violation.getMessage());
    }

    @Test
    void shouldFailValidationWhenCcdCaseIdIsTooLong() {
        // Given
        HomeOfficeStatutoryTimeframeDto dto = HomeOfficeStatutoryTimeframeDto.builder()
            .ccdCaseId("12345678901234567")
            .uan("1234-5678-9012-3456")
            .familyName("Smith")
            .givenNames("John")
            .dateOfBirth(LocalDate.of(1990, 1, 1))
            .stf24weeks(HomeOfficeStatutoryTimeframeDto.Stf24Weeks.builder()
                .status("Yes")
                .cohorts(new String[]{"HU"})
                .build())
            .timeStamp(LocalDateTime.of(2023, 12, 1, 14, 30, 0))
            .build();

        // When
        Set<ConstraintViolation<HomeOfficeStatutoryTimeframeDto>> violations = validator.validate(dto);

        // Then
        assertEquals(1, violations.size());
        ConstraintViolation<HomeOfficeStatutoryTimeframeDto> violation = violations.iterator().next();
        assertEquals("ccdCaseId", violation.getPropertyPath().toString());
        assertEquals("CCD Case ID must be a 16-digit number", violation.getMessage());
    }

    @Test
    void shouldFailValidationWhenCcdCaseIdIsNull() {
        // Given
        HomeOfficeStatutoryTimeframeDto dto = HomeOfficeStatutoryTimeframeDto.builder()
            .ccdCaseId(null)
            .uan("1234-5678-9012-3456")
            .familyName("Smith")
            .givenNames("John")
            .dateOfBirth(LocalDate.of(1990, 1, 1))
            .stf24weeks(HomeOfficeStatutoryTimeframeDto.Stf24Weeks.builder()
                .status("Yes")
                .cohorts(new String[]{"HU"})
                .build())
            .timeStamp(LocalDateTime.of(2023, 12, 1, 14, 30, 0))
            .build();

        // When
        Set<ConstraintViolation<HomeOfficeStatutoryTimeframeDto>> violations = validator.validate(dto);

        // Then
        assertEquals(1, violations.size());
        ConstraintViolation<HomeOfficeStatutoryTimeframeDto> violation = violations.iterator().next();
        assertEquals("ccdCaseId", violation.getPropertyPath().toString());
    }

    @Test
    void shouldPassValidationWithValidUan() {
        // Given
        HomeOfficeStatutoryTimeframeDto dto = HomeOfficeStatutoryTimeframeDto.builder()
            .ccdCaseId("1234567890123456")
            .uan("9876-5432-1098-7654")
            .familyName("Doe")
            .givenNames("Jane")
            .dateOfBirth(LocalDate.of(1985, 5, 15))
            .stf24weeks(HomeOfficeStatutoryTimeframeDto.Stf24Weeks.builder()
                .status("Yes")
                .cohorts(new String[]{"HU"})
                .build())
            .timeStamp(LocalDateTime.of(2024, 1, 15, 10, 20, 30))
            .build();

        // When
        Set<ConstraintViolation<HomeOfficeStatutoryTimeframeDto>> violations = validator.validate(dto);

        // Then
        assertTrue(violations.isEmpty(), "Should have no violations");
    }

    @Test
    void shouldFailValidationWhenUanIsNull() {
        // Given
        HomeOfficeStatutoryTimeframeDto dto = HomeOfficeStatutoryTimeframeDto.builder()
            .ccdCaseId("1234567890123456")
            .uan(null)
            .familyName("Smith")
            .givenNames("John")
            .dateOfBirth(LocalDate.of(1990, 1, 1))
            .stf24weeks(HomeOfficeStatutoryTimeframeDto.Stf24Weeks.builder()
                .status("Yes")
                .cohorts(new String[]{"HU"})
                .build())
            .timeStamp(LocalDateTime.of(2023, 12, 1, 14, 30, 0))
            .build();

        // When
        Set<ConstraintViolation<HomeOfficeStatutoryTimeframeDto>> violations = validator.validate(dto);

        // Then
        assertEquals(1, violations.size());
        ConstraintViolation<HomeOfficeStatutoryTimeframeDto> violation = violations.iterator().next();
        assertEquals("uan", violation.getPropertyPath().toString());
    }

    @Test
    void shouldFailValidationWhenMultipleFieldsAreInvalid() {
        // Given
        HomeOfficeStatutoryTimeframeDto dto = HomeOfficeStatutoryTimeframeDto.builder()
            .ccdCaseId("123")
            .uan("invalid-uan")
            .familyName(null)
            .givenNames(null)
            .dateOfBirth(null)
            .stf24weeks(HomeOfficeStatutoryTimeframeDto.Stf24Weeks.builder()
                .status("Yes")
                .cohorts(new String[]{"HU"})
                .build())
            .timeStamp(null)
            .build();

        // When
        Set<ConstraintViolation<HomeOfficeStatutoryTimeframeDto>> violations = validator.validate(dto);

        // Then
        assertEquals(6, violations.size());
    }
}
