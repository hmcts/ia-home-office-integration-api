package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.testutils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.testutils.StaticPortWiremockFactory.WIREMOCK_PORT;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import ru.lanwen.wiremock.ext.WiremockResolver;
import ru.lanwen.wiremock.ext.WiremockResolver.Wiremock;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.Application;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.testutils.wiremock.GivensBuilder;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.testutils.wiremock.IaCaseApiClient;

@ActiveProfiles("integration")
@SpringBootTest(classes = {
    TestConfiguration.class,
    Application.class
})
@TestPropertySource(properties = {
    "S2S_URL=http://127.0.0.1:" + WIREMOCK_PORT + "/serviceAuth",
    "IDAM_URL=http://127.0.0.1:" + WIREMOCK_PORT + "/userAuth"
})

@ExtendWith({
    WiremockResolver.class
})
@AutoConfigureMockMvc
public class SpringBootIntegrationTest {

    protected GivensBuilder given;
    protected IaCaseApiClient iaCaseApiClient;

    @Autowired
    protected MockMvc mockMvc;

    @BeforeEach
    public void setUpGivens() {
        given = new GivensBuilder();
    }

    @BeforeEach
    public void setUpApiClient() {
        iaCaseApiClient = new IaCaseApiClient(WIREMOCK_PORT);
    }

    @BeforeEach
    public void stubRequests(@Wiremock(factory = StaticPortWiremockFactory.class) WireMockServer server) {

        server.stubFor(get(urlEqualTo("/serviceAuth/details"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", APPLICATION_JSON_VALUE)
                .withBody("ia")));

        server.stubFor(WireMock.post(urlEqualTo("/serviceAuth/lease"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", APPLICATION_JSON_VALUE)
                .withBody("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9."
                          + "eyJzdWIiOiIxMjM0NTY3ODkwIiwiaWF0IjoxNTE2MjM5MDIyfQ."
                          + "L8i6g3PfcHlioHCCPURC9pmXT7gdJpx3kOoyAfNUwCc")));

    }
}
