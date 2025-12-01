package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;


import nl.jqno.equalsverifier.EqualsVerifier;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HomeOfficeStatutoryTimeframeDtoTest {

    private Long ccdCaseId;
    private String uan;
    private String familyName;
    private String givenNames;
    private LocalDate dateOfBirth;
    private boolean hoStatutoryTimeframeStatus;
    private LocalDateTime timeStamp;

    private HomeOfficeStatutoryTimeframeDto homeOfficeStatutoryTimeframeDtoDto;

    @BeforeEach
    void setUp() {
        ccdCaseId = 1234567890123456L;
        uan = "UAN123456";
        familyName = "Smith";
        givenNames = "John";
        dateOfBirth = LocalDate.of(1990, 1, 1);
        hoStatutoryTimeframeStatus = true;
        timeStamp = LocalDateTime.of(2023, 12, 1, 14, 30, 0);
    }

    @Test
    void should_test_equals_contract() {

        EqualsVerifier.simple()
            .forClass(HomeOfficeStatutoryTimeframeDto.class)
            .verify();
    }

    @Test
    void should_hold_onto_values() {

        homeOfficeStatutoryTimeframeDtoDto = HomeOfficeStatutoryTimeframeDto.builder()
            .ccdCaseId(ccdCaseId)
            .uan(uan)
            .familyName(familyName)
            .givenNames(givenNames)
            .dateOfBirth(dateOfBirth)
            .stf24weeks(HomeOfficeStatutoryTimeframeDto.Stf24Weeks.builder()
                .status(hoStatutoryTimeframeStatus ? "Yes" : "No")
                .caseType("HU")
                .build())
            .timeStamp(timeStamp)
            .build();

        assertEquals(ccdCaseId, homeOfficeStatutoryTimeframeDtoDto.getCcdCaseId());
        assertEquals(uan, homeOfficeStatutoryTimeframeDtoDto.getUan());
        assertEquals(familyName, homeOfficeStatutoryTimeframeDtoDto.getFamilyName());
        assertEquals(givenNames, homeOfficeStatutoryTimeframeDtoDto.getGivenNames());
        assertEquals(dateOfBirth, homeOfficeStatutoryTimeframeDtoDto.getDateOfBirth());
        assertEquals(hoStatutoryTimeframeStatus ? "Yes" : "No", homeOfficeStatutoryTimeframeDtoDto.getStf24weeks().getStatus());
        assertEquals("HU", homeOfficeStatutoryTimeframeDtoDto.getStf24weeks().getCaseType());
        assertEquals(timeStamp, homeOfficeStatutoryTimeframeDtoDto.getTimeStamp());
    }

}
