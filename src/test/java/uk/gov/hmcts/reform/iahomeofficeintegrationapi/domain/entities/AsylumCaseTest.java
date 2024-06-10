package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AsylumCaseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private AsylumCase asylumCase;
    private String caseData;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        caseData = "{\"appealReferenceNumber\": \"PA/50222/2019\"}";
        asylumCase = objectMapper.readValue(caseData, AsylumCase.class);
    }

    @Test
    void reads_string() {
        assertThat(asylumCase.read(APPEAL_REFERENCE_NUMBER).get()).isEqualTo("PA/50222/2019");
    }

    @Test
    void reads_using_parameter_type_generics() {
        assertThat(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).get()).isEqualTo("PA/50222/2019");
    }

    @Test
    void writes_simple_type() {
        asylumCase.write(APPEAL_REFERENCE_NUMBER, "some-appeal-reference-number");
        assertThat(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).get()).isEqualTo("some-appeal-reference-number");
    }

    @Test
    void clears_value() {
        asylumCase.clear(APPEAL_REFERENCE_NUMBER);
        assertThat(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).isEmpty();
    }
}