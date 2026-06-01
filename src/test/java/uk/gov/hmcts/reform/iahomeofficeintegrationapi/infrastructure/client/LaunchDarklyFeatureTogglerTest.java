package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.launchdarkly.sdk.LDContext;
import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security.idam.IdentityManagerResponseException;


@ExtendWith(MockitoExtension.class)
class LaunchDarklyFeatureTogglerTest {

    @Mock
    private LDClientInterface ldClient;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private LaunchDarklyFeatureToggler launchDarklyFeatureToggler;

    private LDContext ldContext;

    @Test
    void should_return_default_value_when_key_does_not_exist() {
        when(userDetails.getId()).thenReturn("id");
        when(userDetails.getForename()).thenReturn("forname");
        when(userDetails.getSurname()).thenReturn("surname");
        when(userDetails.getEmailAddress()).thenReturn("emailAddress");
        ldContext = LDContext.builder(userDetails.getId())
            .set("firstName", userDetails.getForename())
            .set("lastName", userDetails.getSurname())
            .set("email", userDetails.getEmailAddress())
            .build();
        String notExistingKey = "not-existing-key";
        when(ldClient.boolVariation(notExistingKey, ldContext, true)).thenReturn(true);

        assertTrue(launchDarklyFeatureToggler.getValue(notExistingKey, true));
    }

    @Test
    void should_return_value_when_key_exists() {
        when(userDetails.getId()).thenReturn("id");
        when(userDetails.getForename()).thenReturn("forname");
        when(userDetails.getSurname()).thenReturn("surname");
        when(userDetails.getEmailAddress()).thenReturn("emailAddress");
        ldContext = LDContext.builder(userDetails.getId())
            .set("firstName", userDetails.getForename())
            .set("lastName", userDetails.getSurname())
            .set("email", userDetails.getEmailAddress())
            .build();
        String existingKey = "existing-key";
        when(ldClient.boolVariation(existingKey, ldContext, false)).thenReturn(true);

        assertTrue(launchDarklyFeatureToggler.getValue(existingKey, false));
    }

    @Test
    void throw_exception_when_user_details_provider_unavailable() {
        when(userDetails.getId()).thenThrow(IdentityManagerResponseException.class);

        assertThatThrownBy(() -> launchDarklyFeatureToggler.getValue("existing-key", true))
            .isExactlyInstanceOf(IdentityManagerResponseException.class);
    }
}

