package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd;

import static junit.framework.TestCase.assertEquals;

import org.junit.jupiter.api.Test;

class NationalityTest {

    @Test
    void has_correct_values_11() {

        assertEquals("Western Sahara", Nationality.EH.toString());
        assertEquals("Yemen", Nationality.YE.toString());
        assertEquals("Zambia", Nationality.ZM.toString());
        assertEquals("Zimbabwe", Nationality.ZW.toString());
    }

    @Test
    void has_correct_values_10() {
        assertEquals("Togo", Nationality.TG.toString());
        assertEquals("Tokelau", Nationality.TK.toString());
        assertEquals("Tonga", Nationality.TO.toString());
        assertEquals("Trinidad and Tobago", Nationality.TT.toString());
        assertEquals("Tunisia", Nationality.TN.toString());
        assertEquals("Turkey", Nationality.TR.toString());
        assertEquals("Turkmenistan", Nationality.TM.toString());
        assertEquals("Turks and Caicos Islands", Nationality.TC.toString());
        assertEquals("Tuvalu", Nationality.TV.toString());
        assertEquals("Uganda", Nationality.UG.toString());
        assertEquals("Ukraine", Nationality.UA.toString());
        assertEquals("United Arab Emirates", Nationality.AE.toString());
        assertEquals("United Kingdom", Nationality.GB.toString());
        assertEquals("United States of America", Nationality.US.toString());
        assertEquals("United States Minor Outlying Islands", Nationality.UM.toString());
        assertEquals("Uruguay", Nationality.UY.toString());
        assertEquals("Uzbekistan", Nationality.UZ.toString());
        assertEquals("Vanuatu", Nationality.VU.toString());
        assertEquals("Venezuela (Bolivarian Republic of)", Nationality.VE.toString());
        assertEquals("Viet Nam", Nationality.VN.toString());
        assertEquals("Virgin Islands, US", Nationality.VI.toString());
        assertEquals("Wallis and Futuna Islands", Nationality.WF.toString());
    }

    @Test
    void has_correct_values_09() {
        assertEquals("Sierra Leone", Nationality.SL.toString());
        assertEquals("Singapore", Nationality.SG.toString());
        assertEquals("Sint Maarten (Dutch part)", Nationality.SX.toString());
        assertEquals("Slovakia", Nationality.SK.toString());
        assertEquals("Slovenia", Nationality.SI.toString());
        assertEquals("Solomon Islands", Nationality.SB.toString());
        assertEquals("Somalia", Nationality.SO.toString());
        assertEquals("South Africa", Nationality.ZA.toString());
        assertEquals("South Georgia and the South Sandwich Islands", Nationality.GS.toString());
        assertEquals("South Sudan", Nationality.SS.toString());
        assertEquals("Spain", Nationality.ES.toString());
        assertEquals("Sri Lanka", Nationality.LK.toString());
        assertEquals("Stateless", Nationality.ZZ.toString());
        assertEquals("Sudan", Nationality.SD.toString());
        assertEquals("Suriname *", Nationality.SR.toString());
        assertEquals("Svalbard and Jan Mayen Islands", Nationality.SJ.toString());
        assertEquals("Swaziland", Nationality.SZ.toString());
        assertEquals("Sweden", Nationality.SE.toString());
        assertEquals("Switzerland", Nationality.CH.toString());
        assertEquals("Syrian Arab Republic (Syria)", Nationality.SY.toString());
        assertEquals("Taiwan", Nationality.TW.toString());
        assertEquals("Tajikistan", Nationality.TJ.toString());
        assertEquals("Tanzania *, United Republic of", Nationality.TZ.toString());
        assertEquals("Thailand", Nationality.TH.toString());
        assertEquals("Timor-Leste", Nationality.TL.toString());
    }

    @Test
    void has_correct_values_08() {
        assertEquals("Peru", Nationality.PE.toString());
        assertEquals("Philippines", Nationality.PH.toString());
        assertEquals("Pitcairn", Nationality.PN.toString());
        assertEquals("Poland", Nationality.PL.toString());
        assertEquals("Portugal", Nationality.PT.toString());
        assertEquals("Puerto Rico", Nationality.PR.toString());
        assertEquals("Qatar", Nationality.QA.toString());
        assertEquals("Réunion", Nationality.RE.toString());
        assertEquals("Romania", Nationality.RO.toString());
        assertEquals("Russian Federation", Nationality.RU.toString());
        assertEquals("Rwanda", Nationality.RW.toString());
        assertEquals("Saint-Barthélemy",Nationality.BL.toString());
        assertEquals("Saint Helena", Nationality.SH.toString());
        assertEquals("Saint Kitts and Nevis", Nationality.KN.toString());
        assertEquals("Saint Lucia", Nationality.LC.toString());
        assertEquals("Saint-Martin (French part)", Nationality.MF.toString());
        assertEquals("Saint Pierre and Miquelon", Nationality.PM.toString());
        assertEquals("Saint Vincent and Grenadines", Nationality.VC.toString());
        assertEquals("Samoa", Nationality.WS.toString());
        assertEquals("San Marino", Nationality.SM.toString());
        assertEquals("Sao Tome and Principe", Nationality.ST.toString());
        assertEquals("Saudi Arabia", Nationality.SA.toString());
        assertEquals("Senegal", Nationality.SN.toString());
        assertEquals("Serbia", Nationality.RS.toString());
        assertEquals("Seychelles", Nationality.SC.toString());
    }

    @Test
    void has_correct_values_07() {
        assertEquals("Montserrat", Nationality.MS.toString());
        assertEquals("Morocco", Nationality.MA.toString());
        assertEquals("Mozambique", Nationality.MZ.toString());
        assertEquals("Myanmar", Nationality.MM.toString());
        assertEquals("Namibia", Nationality.NA.toString());
        assertEquals("Nauru", Nationality.NR.toString());
        assertEquals("Nepal", Nationality.NP.toString());
        assertEquals("Netherlands", Nationality.NL.toString());
        assertEquals("Netherlands Antilles", Nationality.AN.toString());
        assertEquals("New Caledonia", Nationality.NC.toString());
        assertEquals("New Zealand", Nationality.NZ.toString());
        assertEquals("Nicaragua", Nationality.NI.toString());
        assertEquals("Niger", Nationality.NE.toString());
        assertEquals("Nigeria", Nationality.NG.toString());
        assertEquals("Niue", Nationality.NU.toString());
        assertEquals("Norfolk Island", Nationality.NF.toString());
        assertEquals("Northern Mariana Islands", Nationality.MP.toString());
        assertEquals("Norway", Nationality.NO.toString());
        assertEquals("Oman", Nationality.OM.toString());
        assertEquals("Pakistan", Nationality.PK.toString());
        assertEquals("Palau", Nationality.PW.toString());
        assertEquals("Palestinian Territory, Occupied", Nationality.PS.toString());
        assertEquals("Panama", Nationality.PA.toString());
        assertEquals("Papua New Guinea", Nationality.PG.toString());
        assertEquals("Paraguay", Nationality.PY.toString());
    }

    @Test
    void has_correct_values_06() {
        assertEquals("Lebanon", Nationality.LB.toString());
        assertEquals("Lesotho", Nationality.LS.toString());
        assertEquals("Liberia", Nationality.LR.toString());
        assertEquals("Libya", Nationality.LY.toString());
        assertEquals("Liechtenstein", Nationality.LI.toString());
        assertEquals("Lithuania", Nationality.LT.toString());
        assertEquals("Luxembourg", Nationality.LU.toString());
        assertEquals("Macedonia, Republic of", Nationality.MK.toString());
        assertEquals("Madagascar", Nationality.MG.toString());
        assertEquals("Malawi", Nationality.MW.toString());
        assertEquals("Malaysia", Nationality.MY.toString());
        assertEquals("Maldives", Nationality.MV.toString());
        assertEquals("Mali", Nationality.ML.toString());
        assertEquals("Malta", Nationality.MT.toString());
        assertEquals("Marshall Islands", Nationality.MH.toString());
        assertEquals("Martinique", Nationality.MQ.toString());
        assertEquals("Mauritania", Nationality.MR.toString());
        assertEquals("Mauritius", Nationality.MU.toString());
        assertEquals("Mayotte", Nationality.YT.toString());
        assertEquals("Mexico", Nationality.MX.toString());
        assertEquals("Micronesia, Federated States of", Nationality.FM.toString());
        assertEquals("Moldova", Nationality.MD.toString());
        assertEquals("Monaco", Nationality.MC.toString());
        assertEquals("Mongolia", Nationality.MN.toString());
        assertEquals("Montenegro", Nationality.ME.toString());
    }

    @Test
    void has_correct_values_05() {
        assertEquals("Holy See (Vatican City State)", Nationality.VA.toString());
        assertEquals("Honduras", Nationality.HN.toString());
        assertEquals("Hungary", Nationality.HU.toString());
        assertEquals("Iceland", Nationality.IS.toString());
        assertEquals("India", Nationality.IN.toString());
        assertEquals("Indonesia", Nationality.ID.toString());
        assertEquals("Iran, Islamic Republic of", Nationality.IR.toString());
        assertEquals("Iraq", Nationality.IQ.toString());
        assertEquals("Ireland", Nationality.IE.toString());
        assertEquals("Isle of Man", Nationality.IM.toString());
        assertEquals("Israel", Nationality.IL.toString());
        assertEquals("Italy", Nationality.IT.toString());
        assertEquals("Jamaica", Nationality.JM.toString());
        assertEquals("Japan", Nationality.JP.toString());
        assertEquals("Jersey", Nationality.JE.toString());
        assertEquals("Jordan", Nationality.JO.toString());
        assertEquals("Kazakhstan", Nationality.KZ.toString());
        assertEquals("Kenya", Nationality.KE.toString());
        assertEquals("Kiribati", Nationality.KI.toString());
        assertEquals("Korea, Democratic People's Republic of", Nationality.KP.toString());
        assertEquals("Korea, Republic of", Nationality.KR.toString());
        assertEquals("Kuwait", Nationality.KW.toString());
        assertEquals("Kyrgyzstan", Nationality.KG.toString());
        assertEquals("Lao PDR", Nationality.LA.toString());
        assertEquals("Latvia", Nationality.LV.toString());
    }

    @Test
    void has_correct_values_04() {
        assertEquals("Faroe Islands", Nationality.FO.toString());
        assertEquals("Fiji", Nationality.FJ.toString());
        assertEquals("Finland", Nationality.FI.toString());
        assertEquals("France", Nationality.FR.toString());
        assertEquals("French Guiana", Nationality.GF.toString());
        assertEquals("French Polynesia", Nationality.PF.toString());
        assertEquals("French Southern Territories", Nationality.TF.toString());
        assertEquals("Gabon", Nationality.GA.toString());
        assertEquals("Gambia", Nationality.GM.toString());
        assertEquals("Georgia", Nationality.GE.toString());
        assertEquals("Germany", Nationality.DE.toString());
        assertEquals("Ghana", Nationality.GH.toString());
        assertEquals("Gibraltar", Nationality.GI.toString());
        assertEquals("Greece", Nationality.GR.toString());
        assertEquals("Greenland", Nationality.GL.toString());
        assertEquals("Grenada", Nationality.GD.toString());
        assertEquals("Guadeloupe", Nationality.GP.toString());
        assertEquals("Guam", Nationality.GU.toString());
        assertEquals("Guatemala", Nationality.GT.toString());
        assertEquals("Guernsey", Nationality.GG.toString());
        assertEquals("Guinea", Nationality.GN.toString());
        assertEquals("Guinea-Bissau", Nationality.GW.toString());
        assertEquals("Guyana", Nationality.GY.toString());
        assertEquals("Haiti", Nationality.HT.toString());
        assertEquals("Heard Island and Mcdonald Islands", Nationality.HM.toString());
    }

    @Test
    void has_correct_values_03() {
        assertEquals("Cocos (Keeling) Islands", Nationality.CC.toString());
        assertEquals("Colombia", Nationality.CO.toString());
        assertEquals("Comoros", Nationality.KM.toString());
        assertEquals("Congo (Brazzaville)", Nationality.CG.toString());
        assertEquals("Congo, Democratic Republic of the", Nationality.CD.toString());
        assertEquals("Cook Islands", Nationality.CK.toString());
        assertEquals("Costa Rica", Nationality.CR.toString());
        assertEquals("Côte d'Ivoire", Nationality.CI.toString());
        assertEquals("Croatia", Nationality.HR.toString());
        assertEquals("Cuba", Nationality.CU.toString());
        assertEquals("Curaçao", Nationality.CW.toString());
        assertEquals("Cyprus", Nationality.CY.toString());
        assertEquals("Czech Republic", Nationality.CZ.toString());
        assertEquals("Denmark", Nationality.DK.toString());
        assertEquals("Djibouti", Nationality.DJ.toString());
        assertEquals("Dominica", Nationality.DM.toString());
        assertEquals("Dominican Republic", Nationality.DO.toString());
        assertEquals("Ecuador", Nationality.EC.toString());
        assertEquals("Egypt", Nationality.EG.toString());
        assertEquals("El Salvador", Nationality.SV.toString());
        assertEquals("Equatorial Guinea", Nationality.GQ.toString());
        assertEquals("Eritrea", Nationality.ER.toString());
        assertEquals("Estonia", Nationality.EE.toString());
        assertEquals("Ethiopia", Nationality.ET.toString());
        assertEquals("Falkland Islands (Malvinas)", Nationality.FK.toString());
    }

    @Test
    void has_correct_values_02() {
        assertEquals("Bhutan", Nationality.BT.toString());
        assertEquals("Bolivia", Nationality.BO.toString());
        assertEquals("Bonaire, Sint Eustatius and Saba", Nationality.BQ.toString());
        assertEquals("Bosnia and Herzegovina", Nationality.BA.toString());
        assertEquals("Botswana", Nationality.BW.toString());
        assertEquals("Bouvet Island", Nationality.BV.toString());
        assertEquals("Brazil", Nationality.BR.toString());
        assertEquals("British Virgin Islands", Nationality.VG.toString());
        assertEquals("British Indian Ocean Territory", Nationality.IO.toString());
        assertEquals("Brunei Darussalam", Nationality.BN.toString());
        assertEquals("Bulgaria", Nationality.BG.toString());
        assertEquals("Burkina Faso", Nationality.BF.toString());
        assertEquals("Burundi", Nationality.BI.toString());
        assertEquals("Cambodia", Nationality.KH.toString());
        assertEquals("Cameroon", Nationality.CM.toString());
        assertEquals("Canada", Nationality.CA.toString());
        assertEquals("Cape Verde", Nationality.CV.toString());
        assertEquals("Cayman Islands", Nationality.KY.toString());
        assertEquals("Central African Republic", Nationality.CF.toString());
        assertEquals("Chad", Nationality.TD.toString());
        assertEquals("Chile", Nationality.CL.toString());
        assertEquals("China", Nationality.CN.toString());
        assertEquals("Hong Kong, Special Administrative Region of China", Nationality.HK.toString());
        assertEquals("Macao, Special Administrative Region of China", Nationality.MO.toString());
        assertEquals("Christmas Island", Nationality.CX.toString());
    }

    @Test
    void has_correct_values_01() {
        assertEquals("Afghanistan", Nationality.AF.toString());
        assertEquals("Aland Islands", Nationality.AX.toString());
        assertEquals("Albania", Nationality.AL.toString());
        assertEquals("Algeria", Nationality.DZ.toString());
        assertEquals("American Samoa", Nationality.AS.toString());
        assertEquals("Andorra", Nationality.AD.toString());
        assertEquals("Angola", Nationality.AO.toString());
        assertEquals("Antarctica", Nationality.AQ.toString());
        assertEquals("Antigua and Barbuda", Nationality.AG.toString());
        assertEquals("Argentina", Nationality.AR.toString());
        assertEquals("Armenia", Nationality.AM.toString());
        assertEquals("Aruba", Nationality.AW.toString());
        assertEquals("Australia", Nationality.AU.toString());
        assertEquals("Austria", Nationality.AT.toString());
        assertEquals("Anguilla", Nationality.AI.toString());
        assertEquals("Azerbaijan", Nationality.AZ.toString());
        assertEquals("Bahamas", Nationality.BS.toString());
        assertEquals("Bahrain", Nationality.BH.toString());
        assertEquals("Bangladesh", Nationality.BD.toString());
        assertEquals("Barbados", Nationality.BB.toString());
        assertEquals("Belarus", Nationality.BY.toString());
        assertEquals("Belgium", Nationality.BE.toString());
        assertEquals("Belize", Nationality.BZ.toString());
        assertEquals("Benin", Nationality.BJ.toString());
        assertEquals("Bermuda", Nationality.BM.toString());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(252, Nationality.values().length);
    }
}
