package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd;

import static junit.framework.TestCase.assertEquals;

import org.junit.jupiter.api.Test;

class HearingCentreTest {

    @Test
    void has_correct_values() {
        assertEquals("birmingham", HearingCentre.BIRMINGHAM.getId());
        assertEquals("bradford", HearingCentre.BRADFORD.getId());
        assertEquals("coventry", HearingCentre.COVENTRY.getId());
        assertEquals("glasgow", HearingCentre.GLASGOW.getId());
        assertEquals("glasgowTribunalsCentre", HearingCentre.GLASGOW_TRIBUNAL_CENTRE.getId());
        assertEquals("harmondsworth", HearingCentre.HARMONDSWORTH.getId());
        assertEquals("hattonCross", HearingCentre.HATTON_CROSS.getId());
        assertEquals("manchester", HearingCentre.MANCHESTER.getId());
        assertEquals("newport", HearingCentre.NEWPORT.getId());
        assertEquals("northShields", HearingCentre.NORTH_SHIELDS.getId());
        assertEquals("nottingham", HearingCentre.NOTTINGHAM.getId());
        assertEquals("taylorHouse", HearingCentre.TAYLOR_HOUSE.getId());
        assertEquals("newcastle", HearingCentre.NEWCASTLE.getId());
        assertEquals("belfast", HearingCentre.BELFAST.getId());        
        assertEquals("hendon", HearingCentre.HENDON.getId());
        assertEquals("yarlsWood", HearingCentre.YARLS_WOOD.getId());
        assertEquals("bradfordKeighley", HearingCentre.BRADFORD_KEIGHLEY.getId());
        assertEquals("mccMinshull", HearingCentre.MCC_MINSHULL.getId());
        assertEquals("mccCrownSquare", HearingCentre.MCC_CROWN_SQUARE.getId());
        assertEquals("manchesterMags", HearingCentre.MANCHESTER_MAGS.getId());
        assertEquals("nthTyneMags", HearingCentre.NTH_TYNE_MAGS.getId());
        assertEquals("leedsMags", HearingCentre.LEEDS_MAGS.getId());
        assertEquals("alloaSherrif", HearingCentre.ALLOA_SHERRIF.getId());
        assertEquals("remoteHearing", HearingCentre.REMOTE_HEARING.getId());

    }

    @Test
    void can_be_created_from() {
        assertEquals(HearingCentre.fromId("bradford"), HearingCentre.BRADFORD);
        assertEquals(HearingCentre.fromId("birmingham"), HearingCentre.BIRMINGHAM);
        assertEquals(HearingCentre.fromId("coventry"), HearingCentre.COVENTRY);
        assertEquals(HearingCentre.fromId("glasgow"), HearingCentre.GLASGOW);
        assertEquals(HearingCentre.fromId("glasgowTribunalsCentre"), HearingCentre.GLASGOW_TRIBUNAL_CENTRE);
        assertEquals(HearingCentre.fromId("harmondsworth"), HearingCentre.HARMONDSWORTH);
        assertEquals(HearingCentre.fromId("hattonCross"), HearingCentre.HATTON_CROSS);
        assertEquals(HearingCentre.fromId("manchester"), HearingCentre.MANCHESTER);
        assertEquals(HearingCentre.fromId("northShields"), HearingCentre.NORTH_SHIELDS);
        assertEquals(HearingCentre.fromId("newport"), HearingCentre.NEWPORT);
        assertEquals(HearingCentre.fromId("nottingham"), HearingCentre.NOTTINGHAM);
        assertEquals(HearingCentre.fromId("taylorHouse"), HearingCentre.TAYLOR_HOUSE);
        assertEquals(HearingCentre.fromId("newcastle"), HearingCentre.NEWCASTLE);
        assertEquals(HearingCentre.fromId("belfast"), HearingCentre.BELFAST);
        assertEquals(HearingCentre.fromId("hendon"), HearingCentre.HENDON);
        assertEquals(HearingCentre.fromId("yarlsWood"), HearingCentre.YARLS_WOOD);
        assertEquals(HearingCentre.fromId("bradfordKeighley"), HearingCentre.BRADFORD_KEIGHLEY);
        assertEquals(HearingCentre.fromId("mccMinshull"), HearingCentre.MCC_MINSHULL);
        assertEquals(HearingCentre.fromId("mccCrownSquare"), HearingCentre.MCC_CROWN_SQUARE);
        assertEquals(HearingCentre.fromId("manchesterMags"), HearingCentre.MANCHESTER_MAGS);
        assertEquals(HearingCentre.fromId("nthTyneMags"), HearingCentre.NTH_TYNE_MAGS);
        assertEquals(HearingCentre.fromId("leedsMags"), HearingCentre.LEEDS_MAGS);
        assertEquals(HearingCentre.fromId("alloaSherrif"), HearingCentre.ALLOA_SHERRIF);
        assertEquals(HearingCentre.fromId("remoteHearing"), HearingCentre.REMOTE_HEARING);
    }

    @Test
    void has_correct_id_values() {
        assertEquals("Bradford", HearingCentre.BRADFORD.getValue());
        assertEquals("Birmingham", HearingCentre.BIRMINGHAM.getValue());
        assertEquals("Coventry Magistrates Court", HearingCentre.COVENTRY.getValue());
        assertEquals("Glasgow (Eagle Building)", HearingCentre.GLASGOW.getValue());
        assertEquals("Glasgow Tribunals Centre", HearingCentre.GLASGOW_TRIBUNAL_CENTRE.getValue());
        assertEquals("Harmondsworth", HearingCentre.HARMONDSWORTH.getValue());
        assertEquals("Hatton Cross", HearingCentre.HATTON_CROSS.getValue());
        assertEquals("Manchester", HearingCentre.MANCHESTER.getValue());
        assertEquals("North Shields", HearingCentre.NORTH_SHIELDS.getValue());
        assertEquals("Newport", HearingCentre.NEWPORT.getValue());
        assertEquals("Nottingham Justice Centre", HearingCentre.NOTTINGHAM.getValue());
        assertEquals("Taylor House", HearingCentre.TAYLOR_HOUSE.getValue());
        assertEquals("Newcastle Civil & Family Courts and Tribunals Centre", HearingCentre.NEWCASTLE.getValue());
        assertEquals("Belfast", HearingCentre.BELFAST.getValue());
        assertEquals("Hendon", HearingCentre.HENDON.getValue());
        assertEquals("Yarl's Wood", HearingCentre.YARLS_WOOD.getValue());
        assertEquals("Bradford & Keighley", HearingCentre.BRADFORD_KEIGHLEY.getValue());
        assertEquals("MCC Minshull st", HearingCentre.MCC_MINSHULL.getValue());
        assertEquals("MCC Crown Square", HearingCentre.MCC_CROWN_SQUARE.getValue());
        assertEquals("Manchester Mags", HearingCentre.MANCHESTER_MAGS.getValue());
        assertEquals("NTH Tyne Mags", HearingCentre.NTH_TYNE_MAGS.getValue());
        assertEquals("Leeds Mags", HearingCentre.LEEDS_MAGS.getValue());
        assertEquals("Alloa Sherrif Court", HearingCentre.ALLOA_SHERRIF.getValue());
        assertEquals("Remote hearing", HearingCentre.REMOTE_HEARING.getValue());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(24, HearingCentre.values().length);
    }

}
