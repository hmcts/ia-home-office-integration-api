package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import uk.gov.hmcts.reform.iahomeofficeintegrationapi.component.SpringBootIntegrationTest;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.DbUtils;

/**
 * Built-in feature which saves service's swagger specs in temporary directory.
 * Each travis run on master should automatically save and upload (if updated) documentation.
 */
class SwaggerPublisher extends SpringBootIntegrationTest {

    @MockBean
    private DbUtils dbUtils;

    @DisplayName("Generate swagger documentation")
    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    void generateDocs() throws Exception {
        byte[] specs = mockMvc
            .perform(get("/v3/api-docs?group=ia-home-office-integration-api"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsByteArray();

        try (OutputStream outputStream = Files.newOutputStream(Paths.get("/tmp/swagger-specs.json"))) {
            outputStream.write(specs);
        }

    }
}
