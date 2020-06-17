package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.util;

import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class HomeOfficeMessageHeaderCreator {

    private HomeOfficeMessageHeaderCreator() {
    }

    public static String generateUuid() {
        return UUID.randomUUID().toString();
    }

}
