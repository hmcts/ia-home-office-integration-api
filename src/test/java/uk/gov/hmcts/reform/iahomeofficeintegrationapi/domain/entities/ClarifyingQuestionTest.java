package uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class ClarifyingQuestionTest {

    @Test
    void getsQuestion() {
        final String question = "some-question";
        ClarifyingQuestion clarifyingQuestion = new ClarifyingQuestion(question);
        Assertions.assertThat(clarifyingQuestion.getQuestion()).isEqualTo(question);
    }
}
