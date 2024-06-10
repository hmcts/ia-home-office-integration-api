package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security.idam;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class IdamUserDetailsTest {

    private final String accessToken = "access-token";
    private final String id = "1234";
    private final List<String> roles = Arrays.asList("role-1", "role-2");
    private final String emailAddress = "email@example.com";
    private final String forename = "forename";
    private final String surname = "surname";
    private final IdamUserDetails userDetails =
        new IdamUserDetails(
            accessToken,
            id,
            roles,
            emailAddress,
            forename,
            surname
        );

    @Test
    void should_hold_onto_values() {
        assertAll(
            () -> assertEquals(accessToken, userDetails.getAccessToken()),
            () -> assertEquals(id, userDetails.getId()),
            () -> assertEquals(roles, userDetails.getRoles()),
            () -> assertEquals(emailAddress, userDetails.getEmailAddress()),
            () -> assertEquals(forename, userDetails.getForename()),
            () -> assertEquals(surname, userDetails.getSurname())
        );
    }
}
