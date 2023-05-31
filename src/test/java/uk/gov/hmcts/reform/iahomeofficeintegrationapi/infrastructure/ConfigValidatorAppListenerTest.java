package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class ConfigValidatorAppListenerTest {

    @Test
    void throwsExceptionWhenHomeOfficeClientIdIsMissing() {
        // Given
        ConfigValidatorAppListener configValidatorAppListener = new ConfigValidatorAppListener();
        configValidatorAppListener.setClientId(null);

        // When/Then
        assertThrows(IllegalArgumentException.class, configValidatorAppListener::breakOnMissingHomeOfficeSecrets);
    }

    @Test
    void throwsExceptionWhenHomeOfficeClientSecretIsMissing() {
        // Given
        ConfigValidatorAppListener configValidatorAppListener = new ConfigValidatorAppListener();
        configValidatorAppListener.setClientId("client-id");
        configValidatorAppListener.setClientSecret(null);

        // When/Then
        assertThrows(IllegalArgumentException.class, configValidatorAppListener::breakOnMissingHomeOfficeSecrets);
    }

    @Test
    @SuppressWarnings("java:S2699") // suppressing SonarLint warning on assertions as it's ok for this test not to have any
    void runsSuccessfullyWhenHomeOfficeSecretsAreCorrectlySet() {
        // Given
        ConfigValidatorAppListener configValidatorAppListener = new ConfigValidatorAppListener();
        configValidatorAppListener.setClientId("client-id");
        configValidatorAppListener.setClientSecret("secret");

        // When
        configValidatorAppListener.breakOnMissingHomeOfficeSecrets();

        // Then
        // I run successfully till the end
    }

}
