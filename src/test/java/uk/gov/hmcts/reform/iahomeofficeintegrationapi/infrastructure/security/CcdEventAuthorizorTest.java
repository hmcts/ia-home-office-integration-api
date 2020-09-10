package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.entities.ccd.Event;

@ExtendWith(MockitoExtension.class)
public class CcdEventAuthorizorTest {

    @Mock
    private AuthorizedRolesProvider authorizedRolesProvider;

    private String role = "caseworker-ia";
    private Map<String, List<Event>> roleEventAccess = new ImmutableMap.Builder<String, List<Event>>()
        .put(role, newArrayList(Event.UNKNOWN))
        .build();
    private CcdEventAuthorizor ccdEventAuthorizor;

    @Test
    public void should_throw_exception_when_provider_returns_empty_list() {

        ccdEventAuthorizor = new CcdEventAuthorizor(roleEventAccess, authorizedRolesProvider);
        when(authorizedRolesProvider.getRoles()).thenReturn(newHashSet());

        AccessDeniedException thrown = assertThrows(
            AccessDeniedException.class,
            () -> ccdEventAuthorizor.throwIfNotAuthorized(Event.UNKNOWN)
        );

        assertEquals("Event 'unknown' not allowed", thrown.getMessage());
    }

    @Test
    public void throw_access_denied_exception_if_role_not_allowed_access_to_event() {

        ccdEventAuthorizor = new CcdEventAuthorizor(roleEventAccess, authorizedRolesProvider);
        when(authorizedRolesProvider.getRoles()).thenReturn(newHashSet());

        assertThatThrownBy(() -> ccdEventAuthorizor.throwIfNotAuthorized(Event.SUBMIT_APPEAL))
            .hasMessage("Event 'submitAppeal' not allowed")
            .isExactlyInstanceOf(AccessDeniedException.class);

        assertThatThrownBy(() -> ccdEventAuthorizor.throwIfNotAuthorized(Event.SUBMIT_APPEAL))
            .hasMessage("Event 'submitAppeal' not allowed")
            .isExactlyInstanceOf(AccessDeniedException.class);
    }

    @Test
    public void should_throw_exception_when_access_map_for_role_is_empty() {

        Map<String, List<Event>> roleEventAccess = new ImmutableMap.Builder<String, List<Event>>()
            .put(role, newArrayList())
            .build();

        ccdEventAuthorizor = new CcdEventAuthorizor(roleEventAccess, authorizedRolesProvider);

        AccessDeniedException thrown = assertThrows(
            AccessDeniedException.class,
            () -> ccdEventAuthorizor.throwIfNotAuthorized(Event.UNKNOWN)
        );

        assertEquals("Event 'unknown' not allowed", thrown.getMessage());
    }
}
