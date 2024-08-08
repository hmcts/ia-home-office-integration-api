package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.callback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.CaseData;

@ExtendWith(MockitoExtension.class)
public class PreSubmitCallbackResponseTest {

    @Mock private CaseData caseData;

    private PreSubmitCallbackResponse<CaseData> preSubmitCallbackResponse;

    @BeforeEach
    public void setUp() {
        preSubmitCallbackResponse = new PreSubmitCallbackResponse<>(caseData);
    }

    @Test
    public void should_hold_onto_values() {
        assertThat(preSubmitCallbackResponse.getData()).isEqualTo(caseData);
    }

    @Test
    public void data_is_mutable() {
        CaseData newCaseData = mock(CaseData.class);
        preSubmitCallbackResponse.setData(newCaseData);
        assertThat(preSubmitCallbackResponse.getData()).isEqualTo(newCaseData);
    }

    @Test
    public void should_store_distinct_errors() {
        List<String> someErrors = Arrays.asList("error3", "error4");
        List<String> someMoreErrors = Arrays.asList("error4", "error1");

        assertThat(preSubmitCallbackResponse.getErrors()).isEmpty();

        preSubmitCallbackResponse.addErrors(someErrors);
        preSubmitCallbackResponse.addErrors(someMoreErrors);
        preSubmitCallbackResponse.addError("error5");

        assertThat(preSubmitCallbackResponse.getErrors()).containsExactly("error3", "error4", "error1", "error5");
    }
}