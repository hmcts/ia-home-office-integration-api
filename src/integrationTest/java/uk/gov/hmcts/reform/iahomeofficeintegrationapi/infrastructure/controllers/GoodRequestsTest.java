package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.Application;

@ActiveProfiles("integrationTest")
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class GoodRequestsTest {
    private static final String hoUAN = "1111-2222-3333-4444";
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @DisplayName("Should welcome upon root request with 200 response code")
    @Test
    public void check_welcomeRootEndpoint_Returns_200Response() throws Exception {
        MvcResult response = mockMvc.perform(get("/")).andExpect(status().isOk()).andReturn();

        final String message = "Welcome to Home Office Integration API";
        assertThat(response.getResponse().getContentAsString()).contains(message);
    }

    @DisplayName("Should send mock request and get response with 200 response code")
    @Test
    public void check_callback_Endpoint_WithUan_Returns_200Response() throws Exception {

        MvcResult response = mockMvc.perform(
            post("/asylum/ccdAboutToSubmit").contentType(MediaType.APPLICATION_JSON)
                .content(hoUAN))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("ia_home_office_reference").value(hoUAN))
            .andReturn();

        assertThat(response.getResponse().getContentAsString()).contains(
            "ia_home_office_reference:1111-2222-3333-4444");
    }

}
