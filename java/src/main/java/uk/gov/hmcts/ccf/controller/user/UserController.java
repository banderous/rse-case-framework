package uk.gov.hmcts.ccf.controller.user;

import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/web")
public class UserController {

    @GetMapping("/userInfo")
    public UserInfo getUserInfo(
        @Parameter(hidden = true) @AuthenticationPrincipal OidcUser principal) {
        return new UserInfo(principal.getName(),
            principal.getGivenName(),
            principal.getFamilyName(),
            principal.getAuthorities().stream()
                .map(x -> x.getAuthority())
                .collect(Collectors.toSet()));
    }
}
