package uk.gov.hmcts.reform.hois.controllers;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.tomakehurst.wiremock.client.WireMock;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Before;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.hois.Application;

@ActiveProfiles("integrationTest")
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class HoisControllerTest {

    private static final String hoUAN = "1111-2222-3333-4444";
    @Autowired
    private transient MockMvc mockMvc;
    @org.springframework.beans.factory.annotation.Value("classpath:homeoffice-api-response-mock.json")
    private transient Resource resourceResFile;

    @Before
    public void setupHomeOfficeDataStub() throws IOException {

        String hoResponseJson =
            new String(Files.readAllBytes(Paths.get(resourceResFile.getURI())));

        assertThat(hoResponseJson).isNotBlank();

        stubFor(WireMock.post(urlEqualTo("/searchByParameters"))
                    .willReturn(aResponse()
                                    .withStatus(200)
                                    .withHeader("Content-Type", "application/json")
                                    .withBody(hoResponseJson)));
    }

    @DisplayName("Should send mock request and get response with 200 response code")
    @Test
    public void checkHoisEndpointWithUanReturns200Response() throws Exception {

        MvcResult response = mockMvc.perform(
            post("/asylum/hois").contentType(MediaType.APPLICATION_JSON)
                .content(hoUAN))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("ia_home_office_reference").value(hoUAN))
            .andReturn();

        assertThat(response.getResponse().getContentAsString()).contains("\"ia_home_office_reference\":\"1111-2222-3333-4444\"");
    }
}
