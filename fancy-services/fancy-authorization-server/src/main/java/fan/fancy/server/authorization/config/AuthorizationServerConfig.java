package fan.fancy.server.authorization.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import fan.fancy.server.authorization.federated.FederatedIdentityIdTokenCustomizer;
import fan.fancy.server.authorization.handler.FancyAuthenticationEntryPoint;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.UUID;

/**
 * 授权服务器配置类.
 *
 * @author Fan
 */
@Configuration(proxyBeanMethods = false)
@AllArgsConstructor
public class AuthorizationServerConfig {

    private final FancyAuthenticationEntryPoint fancyAuthenticationEntryPoint;

    private static KeyPair generateRsaKey() {
        KeyPair keyPair;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
        return keyPair;
    }

    @Bean
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) {
        // 开启 CORS 配置
        http.cors(Customizer.withDefaults());

        http.oauth2AuthorizationServer(authorizationServer -> {
                    http.securityMatcher(authorizationServer.getEndpointsMatcher());

                    authorizationServer
                            // Enable OpenID Connect 1.0
                            .oidc(Customizer.withDefaults())
                            .authorizationEndpoint(authorizationEndpoint -> authorizationEndpoint
                                    .consentPage("/oauth2/consent"));
                })
                .authorizeHttpRequests((authorize) ->
                        authorize.anyRequest().authenticated()
                );
        // Redirect to the login page when not authenticated from the
        // authorization endpoint
//                .exceptionHandling((exceptions) -> exceptions
//                                .defaultAuthenticationEntryPointFor(
//                                        new LoginUrlAuthenticationEntryPoint("/login"),
//                                        new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
//                                )
//                        .authenticationEntryPoint(new CustomAuthenticationEntryPoint())
//                );
        http.exceptionHandling(configurer -> configurer
                .authenticationEntryPoint(fancyAuthenticationEntryPoint));
        return http.build();
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder) {
        JdbcRegisteredClientRepository registeredClientRepository = new JdbcRegisteredClientRepository(jdbcTemplate);

        TokenSettings tokenSettings = TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofHours(1))
                .reuseRefreshTokens(Boolean.TRUE)
                .refreshTokenTimeToLive(Duration.ofDays(7))
                .build();
        ClientSettings clientSettings = ClientSettings.builder()
                .requireAuthorizationConsent(Boolean.TRUE)
                .build();

        RegisteredClient oidcClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("fancy-gateway")
                .clientSecret(passwordEncoder.encode("123456"))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("http://172.16.63.132:10000/login/oauth2/code/fan")
                .scope("fan")
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .tokenSettings(tokenSettings)
                .clientSettings(clientSettings)
                .build();
        RegisteredClient oidcRegisteredClient = registeredClientRepository.findByClientId(oidcClient.getClientId());
        if (oidcRegisteredClient == null) {
            registeredClientRepository.save(oidcClient);
        }

        RegisteredClient pkceCilet = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("fan")
                // 公共客户端不需要 client_secret
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("http://127.0.0.1:5173/oauth2")
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .scope("fan")
                // 启用 PKCE
                .clientSettings(ClientSettings.builder()
                        .requireProofKey(Boolean.TRUE)
                        .requireAuthorizationConsent(Boolean.TRUE)
                        .build())
                .build();
        RegisteredClient pkceRegisteredClient = registeredClientRepository.findByClientId(pkceCilet.getClientId());
        if (pkceRegisteredClient == null) {
            registeredClientRepository.save(pkceCilet);
        }
        return registeredClientRepository;
    }

    @Bean
    public JdbcOAuth2AuthorizationService jdbcOAuth2AuthorizationService(JdbcTemplate jdbcTemplate, RegisteredClientRepository registeredClientRepository) {
        return new JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository);
    }

    @Bean
    public JdbcOAuth2AuthorizationConsentService jdbcOAuth2AuthorizationConsentService(JdbcTemplate jdbcTemplate, RegisteredClientRepository registeredClientRepository) {
        // Will be used by the ConsentController
        return new JdbcOAuth2AuthorizationConsentService(jdbcTemplate, registeredClientRepository);
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();

        JWKSet jwkSet = new JWKSet(rsaKey);
        return (jwkSelector, _) -> jwkSelector.select(jwkSet);
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer("http://127.0.0.1:10100")
                .build();
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtEncodingContextOAuth2TokenCustomizer() {
        return new FederatedIdentityIdTokenCustomizer();
    }
}
