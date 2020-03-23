package uk.gov.hmcts.reform.hois.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.hois.Application;
import uk.gov.hmcts.reform.hois.domain.entities.HomeOfficeRequest;

@ActiveProfiles("integrationTest")
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class HoisControllerTest {
    @Autowired
    private transient MockMvc mockMvc;

    @Autowired
    private transient ObjectMapper objectMapper;

    @DisplayName("Should send mock request and get response with 200 response code")
    @Test
    public void checkHoisEndpoint() throws Exception {
        HomeOfficeRequest request = new HomeOfficeRequest();
        request.setHoReference("1234-5678-6789-7890");

        MvcResult response = mockMvc.perform(
            post("/asylum/hois").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("ho_reference").value("1234-5678-6789-7890"))
            .andExpect(jsonPath("appeal_decision_sent_date").value("20-02-2020"))
            .andReturn();

        assertThat(response.getResponse().getContentAsString()).contains("\"ho_reference\":\"1234-5678-6789-7890\"");
    }
}
