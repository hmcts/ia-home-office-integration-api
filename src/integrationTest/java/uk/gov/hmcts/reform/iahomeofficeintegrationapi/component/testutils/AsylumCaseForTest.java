package uk.gov.hmcts.reform.iahomeofficeintegrationapi.component.testutils;

import java.util.Map;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCaseDefinition;

public class AsylumCaseForTest implements Builder<AsylumCase> {

    private AsylumCase asylumCase = new AsylumCase();

    public static AsylumCaseForTest anAsylumCase() {
        return new AsylumCaseForTest();
    }

    public AsylumCaseForTest withCaseDetails(AsylumCase asylumCase) {
        this.asylumCase.putAll(asylumCase);
        return this;
    }

    public <T> AsylumCaseForTest with(AsylumCaseDefinition field, T value) {
        asylumCase.write(field, value);
        return this;
    }

    public AsylumCaseForTest writeOrOverwrite(Map<String, Object> additionalAsylumCaseData) {
        asylumCase.putAll(additionalAsylumCaseData);
        return this;
    }

    public AsylumCase build() {
        return asylumCase;
    }
}
