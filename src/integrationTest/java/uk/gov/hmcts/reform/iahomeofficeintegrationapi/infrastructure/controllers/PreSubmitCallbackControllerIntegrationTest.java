package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.Application;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.testutils.SpringBootIntegrationTest;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PreSubmitCallbackControllerIntegrationTest extends SpringBootIntegrationTest {

    private static final String SERVICE_JWT_TOKEN =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"
        + ".eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ"
        + ".SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

    private static final String homeOffice_UAN = "1111-2222-3333-4444";

    @DisplayName("Should send mock request and get response with 200 response code")
    @Test
    public void check_callback_Endpoint_WithUan_Returns_200Response() throws Exception {

        MvcResult response = mockMvc.perform(
            post("/asylum/ccdAboutToSubmit")
                .header("ServiceAuthorization", SERVICE_JWT_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(homeOffice_UAN))

            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("ia_home_office_reference").value(homeOffice_UAN))
            .andReturn();

        assertThat(response.getResponse().getContentAsString()).contains(
            "ia_home_office_reference:1111-2222-3333-4444");
    }

}
