package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;


import nl.jqno.equalsverifier.EqualsVerifier;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class HomeOfficeStatutoryTimeframeDtoTest {

    private final String id = "id";
    private final String ccdCaseNumber = "1627506765384548";
    private final String uan = "1234-2345-3456-4567";
    private final String familyName = "Goode";
    private final String givenNames = "Ebeneezer Alan";
    private final LocalDate dateOfBirth = LocalDate.parse("1954-06-07");
    private final boolean hoStatutoryTimeframeStatus = true;
    private final LocalDate timeStamp = LocalDate.parse("2023-01-01");

    private HomeOfficeStatutoryTimeframeDto homeOfficeStatutoryTimeframeDtoDto;

    @Test
    void should_test_equals_contract() {

        EqualsVerifier.simple()
            .forClass(HomeOfficeStatutoryTimeframeDto.class)
            .verify();
    }

    @Test
    void should_hold_onto_values() {

        homeOfficeStatutoryTimeframeDtoDto = HomeOfficeStatutoryTimeframeDto.builder()
            .id(id)
            .ccdCaseNumber(ccdCaseNumber)
            .uan(uan)
            .familyName(familyName)
            .givenNames(givenNames)
            .dateOfBirth(dateOfBirth)
            .hoStatutoryTimeframeStatus(hoStatutoryTimeframeStatus)
            .timeStamp(timeStamp)
            .build();

        assertEquals(id, homeOfficeStatutoryTimeframeDtoDto.getId());
        assertEquals(ccdCaseNumber, homeOfficeStatutoryTimeframeDtoDto.getCcdCaseNumber());
        assertEquals(uan, homeOfficeStatutoryTimeframeDtoDto.getUan());
        assertEquals(familyName, homeOfficeStatutoryTimeframeDtoDto.getFamilyName());
        assertEquals(givenNames, homeOfficeStatutoryTimeframeDtoDto.getGivenNames());
        assertEquals(dateOfBirth, homeOfficeStatutoryTimeframeDtoDto.getDateOfBirth());
        assertEquals(hoStatutoryTimeframeStatus, homeOfficeStatutoryTimeframeDtoDto.isHoStatutoryTimeframeStatus());
        assertEquals(timeStamp, homeOfficeStatutoryTimeframeDtoDto.getTimeStamp());
    }

}
