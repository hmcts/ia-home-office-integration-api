package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.controllers;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class PreSubmitCallbackControllerTest {

    private static final String hoUAN = "1111-2222-3333-4444";
    private final PreSubmitCallbackController controller = new PreSubmitCallbackController();

    @Test
    public void should_call_with_homeoffice_reference_return_value() {
        String response = controller.ccdAboutToSubmit(hoUAN).toString();

        assertThat(response.contains(hoUAN));

    }
}
