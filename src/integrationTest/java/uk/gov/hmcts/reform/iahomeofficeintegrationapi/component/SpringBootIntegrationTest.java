package uk.gov.hmcts.reform.iahomeofficeintegrationapi.component;

import static uk.gov.hmcts.reform.iahomeofficeintegrationapi.component.testutils.StaticPortWiremockFactory.WIREMOCK_PORT;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import ru.lanwen.wiremock.ext.WiremockResolver;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.Application;

@SpringBootTest(classes = {
    Application.class
})
@TestPropertySource(properties = {
    "CCD_URL=http://127.0.0.1:" + WIREMOCK_PORT + "/ccd",
    "IDAM_URL=http://127.0.0.1:" + WIREMOCK_PORT + "/idam",
    "S2S_URL=http://127.0.0.1:" + WIREMOCK_PORT + "/s2s",
    "IA_IDAM_CLIENT_ID=ia",
    "IA_IDAM_SECRET=something",
    "IA_HOMEOFFICE_CLIENT_ID=ho-client-id",
    "IA_HOMEOFFICE_SECRET=something",
    "HOME_OFFICE_ENDPOINT=http://127.0.0.1:" + WIREMOCK_PORT
})
@ExtendWith({
    WiremockResolver.class
})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("integration")
public class SpringBootIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

}
