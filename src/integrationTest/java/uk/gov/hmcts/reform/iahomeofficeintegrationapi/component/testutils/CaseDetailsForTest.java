package uk.gov.hmcts.reform.iahomeofficeintegrationapi.component.testutils;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.State;

@Data
public class CaseDetailsForTest {

    @JsonProperty
    private long id;
    @JsonProperty
    private String jurisdiction;
    @JsonProperty
    private State state;
    @JsonProperty("case_data")
    private AsylumCase caseData;

    CaseDetailsForTest(long id, String jurisdiction, State state, AsylumCase caseData) {
        this.id = id;
        this.jurisdiction = jurisdiction;
        this.state = state;
        this.caseData = caseData;
    }

    public static class CaseDetailsForTestBuilder implements Builder<CaseDetailsForTest> {

        public static CaseDetailsForTestBuilder someCaseDetailsWith() {
            return new CaseDetailsForTestBuilder();
        }

        private long id = 1;
        private String jurisdiction = "ia";
        private State state;
        private AsylumCase caseData;

        CaseDetailsForTestBuilder() {
        }

        public CaseDetailsForTestBuilder id(long id) {
            this.id = id;
            return this;
        }

        public CaseDetailsForTestBuilder jurisdiction(String jurisdiction) {
            this.jurisdiction = jurisdiction;
            return this;
        }

        public CaseDetailsForTestBuilder state(State state) {
            this.state = state;
            return this;
        }

        public CaseDetailsForTestBuilder caseData(AsylumCaseForTest caseData) {
            this.caseData = caseData.build();
            return this;
        }

        public CaseDetailsForTest build() {
            return new CaseDetailsForTest(id, jurisdiction, state, caseData);
        }

        public String toString() {
            return "CaseDetailsForTest.CaseDetailsForTestBuilder(id="
                   + this.id + ", jurisdiction="
                   + this.jurisdiction + ", state="
                   + this.state + ", caseData="
                   + this.caseData + ")";
        }
    }
}
