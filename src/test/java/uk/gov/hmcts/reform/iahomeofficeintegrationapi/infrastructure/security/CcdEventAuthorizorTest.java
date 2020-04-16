package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event;

@ExtendWith(MockitoExtension.class)
public class CcdEventAuthorizorTest {

    @Mock
    private UserDetailsProvider userDetailsProvider;

    @Mock
    private UserDetails userDetails;

    private CcdEventAuthorizor ccdEventAuthorizor;

    @BeforeEach
    public void setUp() {

        ccdEventAuthorizor =
            new CcdEventAuthorizor(
                ImmutableMap
                    .<String, List<Event>>builder()
                    .put("caseworker-role", Arrays.asList(Event.REQUEST_RESPONDENT_REVIEW, Event.SEND_DIRECTION))
                    .put("legal-role", Arrays.asList(Event.SUBMIT_APPEAL, Event.BUILD_CASE))
                    .build(),
                userDetailsProvider
            );

        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);
    }

    @Test
    public void does_not_throw_access_denied_exception_if_role_is_allowed_access_to_event() {

        when(userDetails.getRoles()).thenReturn(
            Arrays.asList("some-unrelated-role", "legal-role")
        );

        assertThatCode(() -> ccdEventAuthorizor.throwIfNotAuthorized(Event.BUILD_CASE))
            .doesNotThrowAnyException();

        when(userDetails.getRoles()).thenReturn(
            Arrays.asList("caseworker-role", "some-unrelated-role")
        );

        assertThatCode(() -> ccdEventAuthorizor.throwIfNotAuthorized(Event.SEND_DIRECTION))
            .doesNotThrowAnyException();
    }

    @Test
    public void throw_access_denied_exception_if_role_not_allowed_access_to_event() {

        when(userDetails.getRoles()).thenReturn(
            Arrays.asList("caseworker-role", "some-unrelated-role")
        );

        assertThatThrownBy(() -> ccdEventAuthorizor.throwIfNotAuthorized(Event.BUILD_CASE))
            .hasMessage("Event 'buildCase' not allowed")
            .isExactlyInstanceOf(AccessDeniedException.class);

        when(userDetails.getRoles()).thenReturn(
            Arrays.asList("some-unrelated-role", "legal-role")
        );

        assertThatThrownBy(() -> ccdEventAuthorizor.throwIfNotAuthorized(Event.SEND_DIRECTION))
            .hasMessage("Event 'sendDirection' not allowed")
            .isExactlyInstanceOf(AccessDeniedException.class);
    }

    @Test
    public void throw_access_denied_exception_if_event_not_configured() {

        when(userDetails.getRoles()).thenReturn(
            Arrays.asList("caseworker-role", "some-unrelated-role")
        );

        assertThatThrownBy(() -> ccdEventAuthorizor.throwIfNotAuthorized(Event.UPLOAD_RESPONDENT_EVIDENCE))
            .hasMessage("Event 'uploadRespondentEvidence' not allowed")
            .isExactlyInstanceOf(AccessDeniedException.class);
    }

    @Test
    public void throw_access_denied_exception_if_user_has_no_roles() {

        when(userDetails.getRoles()).thenReturn(
            Collections.emptyList()
        );

        assertThatThrownBy(() -> ccdEventAuthorizor.throwIfNotAuthorized(Event.BUILD_CASE))
            .hasMessage("Event 'buildCase' not allowed")
            .isExactlyInstanceOf(AccessDeniedException.class);
    }
}
