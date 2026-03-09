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
            .hmctsReferenceNumber("PA/12345/2026")
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
    void shouldFailValidationWhenHmctsRefNumIsTooShort() {
        // Given
        HomeOfficeStatutoryTimeframeDto dto = HomeOfficeStatutoryTimeframeDto.builder()
            .hmctsReferenceNumber("PA/1234/2026")
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
        assertEquals("hmctsReferenceNumber", violation.getPropertyPath().toString());
        assertEquals("Home Office reference ID must be of the form XX/12345/2026, where XX is the appeal type, " + 
                     "12345 stands for any five-digit number and 2026 is the year", violation.getMessage());
    }

    @Test
    void shouldFailValidationWhenHmctsRefNumIsTooLong() {
        // Given
        HomeOfficeStatutoryTimeframeDto dto = HomeOfficeStatutoryTimeframeDto.builder()
            .hmctsReferenceNumber("PA/12345/20206")
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
        assertEquals("hmctsReferenceNumber", violation.getPropertyPath().toString());
        assertEquals("Home Office reference ID must be of the form XX/12345/2026, where XX is the appeal type, " + 
                     "12345 stands for any five-digit number and 2026 is the year", violation.getMessage());
    }

    @Test
    void shouldFailValidationWhenHmctsRefNumIsNull() {
        // Given
        HomeOfficeStatutoryTimeframeDto dto = HomeOfficeStatutoryTimeframeDto.builder()
            .hmctsReferenceNumber(null)
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
        assertEquals("hmctsReferenceNumber", violation.getPropertyPath().toString());
    }

    @Test
    void shouldPassValidationWithValidUan() {
        // Given
        HomeOfficeStatutoryTimeframeDto dto = HomeOfficeStatutoryTimeframeDto.builder()
            .hmctsReferenceNumber("PA/12345/2026")
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
    void shouldFailValidationWhenMultipleFieldsAreInvalid() {
        // Given
        HomeOfficeStatutoryTimeframeDto dto = HomeOfficeStatutoryTimeframeDto.builder()
            .hmctsReferenceNumber("123")
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
