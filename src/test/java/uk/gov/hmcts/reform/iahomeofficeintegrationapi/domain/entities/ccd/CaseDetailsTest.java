package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.RequiredFieldMissingException;

class CaseDetailsTest {

    private final long id = 123L;
    private final String jurisdiction = "IA";
    private final State state = State.APPEAL_STARTED;
    private final CaseData caseData = mock(CaseData.class);
    private final LocalDateTime createdDate = LocalDateTime.parse("2019-01-31T11:22:33");

    private CaseDetails<CaseData> caseDetails;

    @BeforeEach
    void setUp() {
        caseDetails = new CaseDetails<>(
                id,
                jurisdiction,
                state,
                caseData,
                createdDate
        );
    }

    @Test
    void should_hold_onto_values() {
        assertThat(caseDetails.getId()).isEqualTo(id);
        assertThat(caseDetails.getJurisdiction()).isEqualTo(jurisdiction);
        assertThat(caseDetails.getState()).isEqualTo(state);
        assertThat(caseDetails.getCaseData()).isEqualTo(caseData);
        assertThat(caseDetails.getCreatedDate()).isEqualTo(createdDate);
    }

    @Test
    void should_throw_required_field_missing_exception() {

        CaseDetails<CaseData> unpopulatedCaseDetails = new CaseDetails<>(
                id,
                null,
                null,
                null,
                null
        );

        assertThatThrownBy(unpopulatedCaseDetails::getJurisdiction)
                .isExactlyInstanceOf(RequiredFieldMissingException.class)
                .hasMessageContaining("jurisdiction");

        assertThatThrownBy(unpopulatedCaseDetails::getState)
                .isExactlyInstanceOf(RequiredFieldMissingException.class)
                .hasMessageContaining("state");

        assertThatThrownBy(unpopulatedCaseDetails::getCaseData)
                .isExactlyInstanceOf(RequiredFieldMissingException.class)
                .hasMessageContaining("caseData");

        assertThatThrownBy(unpopulatedCaseDetails::getCreatedDate)
                .isExactlyInstanceOf(RequiredFieldMissingException.class)
                .hasMessageContaining("createdDate");
    }
}