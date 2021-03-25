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
        assertEquals("hattonCross", HearingCentre.HATTON_CROSS.getId());
        assertEquals("manchester", HearingCentre.MANCHESTER.getId());
        assertEquals("newport", HearingCentre.NEWPORT.getId());
        assertEquals("northShields", HearingCentre.NORTH_SHIELDS.getId());
        assertEquals("nottingham", HearingCentre.NOTTINGHAM.getId());
        assertEquals("taylorHouse", HearingCentre.TAYLOR_HOUSE.getId());
        assertEquals("newcastle", HearingCentre.NEWCASTLE.getId());
        assertEquals("remoteHearing", HearingCentre.REMOTE_HEARING.getId());

    }

    @Test
    void can_be_created_from() {
        assertEquals(HearingCentre.fromId("bradford"), HearingCentre.BRADFORD);
        assertEquals(HearingCentre.fromId("birmingham"), HearingCentre.BIRMINGHAM);
        assertEquals(HearingCentre.fromId("coventry"), HearingCentre.COVENTRY);
        assertEquals(HearingCentre.fromId("glasgow"), HearingCentre.GLASGOW);
        assertEquals(HearingCentre.fromId("glasgowTribunalsCentre"), HearingCentre.GLASGOW_TRIBUNAL_CENTRE);
        assertEquals(HearingCentre.fromId("hattonCross"), HearingCentre.HATTON_CROSS);
        assertEquals(HearingCentre.fromId("manchester"), HearingCentre.MANCHESTER);
        assertEquals(HearingCentre.fromId("northShields"), HearingCentre.NORTH_SHIELDS);
        assertEquals(HearingCentre.fromId("newport"), HearingCentre.NEWPORT);
        assertEquals(HearingCentre.fromId("nottingham"), HearingCentre.NOTTINGHAM);
        assertEquals(HearingCentre.fromId("taylorHouse"), HearingCentre.TAYLOR_HOUSE);
        assertEquals(HearingCentre.fromId("newcastle"), HearingCentre.NEWCASTLE);
        assertEquals(HearingCentre.fromId("remoteHearing"), HearingCentre.REMOTE_HEARING);
    }

    @Test
    void has_correct_id_values() {
        assertEquals("Bradford", HearingCentre.BRADFORD.getValue());
        assertEquals("Birmingham", HearingCentre.BIRMINGHAM.getValue());
        assertEquals("Coventry Magistrates Court", HearingCentre.COVENTRY.getValue());
        assertEquals("Glasgow (Eagle Building)", HearingCentre.GLASGOW.getValue());
        assertEquals("Glasgow Tribunals Centre", HearingCentre.GLASGOW_TRIBUNAL_CENTRE.getValue());
        assertEquals("Hatton Cross", HearingCentre.HATTON_CROSS.getValue());
        assertEquals("Manchester", HearingCentre.MANCHESTER.getValue());
        assertEquals("North Shields", HearingCentre.NORTH_SHIELDS.getValue());
        assertEquals("Newport", HearingCentre.NEWPORT.getValue());
        assertEquals("Nottingham Justice Centre", HearingCentre.NOTTINGHAM.getValue());
        assertEquals("Taylor House", HearingCentre.TAYLOR_HOUSE.getValue());
        assertEquals("Newcastle Civil & Family Courts and Tribunals Centre", HearingCentre.NEWCASTLE.getValue());
        assertEquals("Remote hearing", HearingCentre.REMOTE_HEARING.getValue());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(13, HearingCentre.values().length);
    }

}
