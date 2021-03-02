package uk.gov.hmcts.ccf.config;

import feign.Client;
import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.slf4j.Slf4jLogger;
import org.jooq.generated.tables.records.UsersRecord;
import org.jooq.impl.DefaultDSLContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.jooq.generated.tables.Users.USERS;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.ACCESS_TOKEN;
import static org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames.ID_TOKEN;

@Component
public class JwtAuthorityExtractor extends JwtAuthenticationConverter {

    private final ClientRegistrationRepository clientRegistrationRepository;

    private final Client httpClient;
    private final DefaultDSLContext jooq;

    public JwtAuthorityExtractor(ClientRegistrationRepository clientRegistrationRepository, Client httpClient,
                                 DefaultDSLContext context) {
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.httpClient = httpClient;
        this.jooq = context;
    }

    @Override
    protected Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (jwt.containsClaim("tokenName")) {
            if (jwt.getClaim("tokenName").equals(ACCESS_TOKEN)) {
                authorities = extractAuthorityFromClaims(getUserInfo(jwt.getTokenValue()));
            } else if (jwt.getClaim("tokenName").equals(ID_TOKEN)) {
                authorities = extractAuthorityFromClaims(jwt.getClaims());
            }
        }
        return authorities;
    }

    public Map<String, Object> getUserInfo(String authorization) {
        ClientRegistration registration = clientRegistrationRepository.findByRegistrationId("oidc");
        String userInfoEndpointUri = registration.getProviderDetails()
                .getUserInfoEndpoint().getUri();
        Map<String, Object> result = buildFeignClient(userInfoEndpointUri.replace("/userinfo", ""))
            .userInfo("Bearer " + authorization);
        UsersRecord u = jooq.newRecord(USERS);
        u.setUserId(String.valueOf(result.get("sub")));
        u.setUserForename(String.valueOf(result.get("given_name")));
        u.setUserSurname(String.valueOf(result.get("family_name")));
        u.merge();
        return result;
    }

    private UserInfoClient buildFeignClient(String target) {
        return Feign.builder()
                .client(httpClient)
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .logger(new Slf4jLogger(UserInfoClient.class))
                .target(UserInfoClient.class, target);
    }

    private static Stream<String> getAuthorities(Authentication authentication) {
        Collection<? extends GrantedAuthority> authorities = authentication instanceof JwtAuthenticationToken
            ? extractAuthorityFromClaims(((JwtAuthenticationToken) authentication).getToken().getClaims())
            : authentication.getAuthorities();
        return authorities.stream()
            .map(GrantedAuthority::getAuthority);
    }

    public static List<GrantedAuthority> extractAuthorityFromClaims(Map<String, Object> claims) {
        return ((List<String>) claims.get("roles"))
            .stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
    }

}
