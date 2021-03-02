package uk.gov.hmcts

import org.mockito.Mockito
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.transaction.annotation.Transactional
import spock.lang.Specification
import uk.gov.hmcts.ccf.config.UserProvider

@Transactional
@SpringBootTest
class BaseSpringBootSpec extends Specification {

    // Mock out OIDC client registration.
    @MockBean
    ClientRegistrationRepository registrations;

    @MockBean
    JwtDecoder decoder;

    @MockBean
    UserProvider user

    def setup() {
        Mockito.when(user.getCurrentUserId()).thenReturn(getUserId())
    }

    protected String getUserId() {
        return "1";
    }
}
