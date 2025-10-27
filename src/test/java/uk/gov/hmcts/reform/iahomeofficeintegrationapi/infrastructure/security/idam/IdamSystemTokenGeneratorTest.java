package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security.idam;

import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.domain.service.IdamService;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.model.idam.Token;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class IdamSystemTokenGeneratorTest {

    @Mock
    private IdamService idamService;

    private IdamSystemTokenGenerator idamSystemTokenGenerator;

    @BeforeEach
    void setUp() {
        idamSystemTokenGenerator = new IdamSystemTokenGenerator(idamService);
    }

    @Test
    void should_return_access_token_when_service_call_succeeds() {
        Token token = mock(Token.class);
        when(token.getAccessToken()).thenReturn("test-access-token");
        when(idamService.getServiceUserToken()).thenReturn(token);

        String result = idamSystemTokenGenerator.generate();

        assertEquals("test-access-token", result);
    }

    @Test
    void should_throw_identity_manager_response_exception_when_feign_exception_occurs() {
        FeignException feignException = mock(FeignException.class);
        when(idamService.getServiceUserToken()).thenThrow(feignException);

        assertThrows(IdentityManagerResponseException.class, 
            () -> idamSystemTokenGenerator.generate());
    }
}