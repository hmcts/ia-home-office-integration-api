package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HomeOfficeStatutoryTimeframeBaseTest {

    private static final String VALID_HMCTS_REF = "PA/12345/2026";
    private static final String INVALID_HMCTS_REF = "INVALID/REF";
    private static final String VALID_UAN = "1234-5678-9012-3456";

    private String familyName;
    private String givenNames;
    private LocalDate dateOfBirth;
    private OffsetDateTime timeStamp;

    private Validator validator;

    @BeforeEach
    void setUp() {
        familyName = "Smith";
        givenNames = "John";
        dateOfBirth = LocalDate.of(1990, 1, 1);
        timeStamp = OffsetDateTime.of(2023, 12, 1, 14, 30, 0, 0, ZoneOffset.UTC);

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void should_satisfy_equals_and_hashcode_contract() {
        EqualsVerifier.simple()
            .forClass(TestImplementation.class)
            .verify();
    }

    @Test
    void should_build_and_hold_values() {

        TestImplementation instance = TestImplementation.builder()
            .hmctsReferenceNumber(VALID_HMCTS_REF)
            .uan(VALID_UAN)
            .familyName(familyName)
            .givenNames(givenNames)
            .dateOfBirth(dateOfBirth)
            .timeStamp(timeStamp)
            .build();

        assertEquals(VALID_HMCTS_REF, instance.getHmctsReferenceNumber());
        assertEquals(VALID_UAN, instance.getUan());
        assertEquals(familyName, instance.getFamilyName());
        assertEquals(givenNames, instance.getGivenNames());
        assertEquals(dateOfBirth, instance.getDateOfBirth());
        assertEquals(timeStamp, instance.getTimeStamp());
    }

    @Test
    void should_copy_constructor_clone_values() {

        TestImplementation original = TestImplementation.builder()
            .hmctsReferenceNumber(VALID_HMCTS_REF)
            .uan(VALID_UAN)
            .familyName(familyName)
            .givenNames(givenNames)
            .dateOfBirth(dateOfBirth)
            .timeStamp(timeStamp)
            .build();

        TestImplementation copy = new TestImplementation(original);

        assertEquals(original.getHmctsReferenceNumber(), copy.getHmctsReferenceNumber());
        assertEquals(original.getUan(), copy.getUan());
        assertEquals(original.getFamilyName(), copy.getFamilyName());
        assertEquals(original.getGivenNames(), copy.getGivenNames());
        assertEquals(original.getDateOfBirth(), copy.getDateOfBirth());
        assertEquals(original.getTimeStamp(), copy.getTimeStamp());
    }

    @Test
    void should_pass_validation_when_valid() {

        TestImplementation instance = TestImplementation.builder()
            .hmctsReferenceNumber(VALID_HMCTS_REF)
            .uan(VALID_UAN)
            .familyName(familyName)
            .givenNames(givenNames)
            .dateOfBirth(dateOfBirth)
            .timeStamp(timeStamp)
            .build();

        Set<ConstraintViolation<TestImplementation>> violations = validator.validate(instance);

        assertTrue(violations.isEmpty());
    }

    @Test
    void should_fail_validation_when_hmcts_reference_invalid() {

        TestImplementation instance = TestImplementation.builder()
            .hmctsReferenceNumber(INVALID_HMCTS_REF)
            .uan(VALID_UAN)
            .familyName(familyName)
            .givenNames(givenNames)
            .dateOfBirth(dateOfBirth)
            .timeStamp(timeStamp)
            .build();

        Set<ConstraintViolation<TestImplementation>> violations = validator.validate(instance);

        assertFalse(violations.isEmpty());
    }

    @Test
    void should_fail_validation_when_required_fields_null() {

        TestImplementation instance = TestImplementation.builder().build();

        Set<ConstraintViolation<TestImplementation>> violations = validator.validate(instance);

        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("hmctsReferenceNumber")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("familyName")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("givenNames")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("dateOfBirth")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("timeStamp")));
    }

    @Test
    void should_allow_null_optional_fields() {

        TestImplementation instance = TestImplementation.builder()
            .hmctsReferenceNumber(VALID_HMCTS_REF)
            .familyName(familyName)
            .givenNames(givenNames)
            .dateOfBirth(dateOfBirth)
            .timeStamp(timeStamp)
            .build();

        Set<ConstraintViolation<TestImplementation>> violations = validator.validate(instance);

        assertTrue(violations.isEmpty());
        assertEquals(null, instance.getUan());
    }

    /**
     * Concrete implementation for testing abstract base class
     */
    @lombok.experimental.SuperBuilder
    @lombok.Data
    @lombok.EqualsAndHashCode(callSuper = true)
    static class TestImplementation extends HomeOfficeStatutoryTimeframeBase {

        public TestImplementation() {
            super();
        }

        public TestImplementation(HomeOfficeStatutoryTimeframeBase other) {
            super(other);
        }
    }
}