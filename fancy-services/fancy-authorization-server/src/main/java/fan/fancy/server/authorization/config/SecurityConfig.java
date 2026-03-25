package fan.fancy.server.authorization.config;

import fan.fancy.server.authorization.handler.FancyAuthenticationEntryPoint;
import fan.fancy.server.authorization.handler.FancyLoginFailureHandler;
import fan.fancy.server.authorization.handler.FancyLoginSuccessHandler;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 配置类.
 *
 * @author Fan
 */
@EnableWebSecurity
@Configuration(proxyBeanMethods = false)
@AllArgsConstructor
public class SecurityConfig {

    private final FancyAuthenticationEntryPoint fancyAuthenticationEntryPoint;

    private final FancyLoginSuccessHandler fancyLoginSuccessHandler;

    private final FancyLoginFailureHandler fancyLoginFailureHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http.authorizeHttpRequests(registry -> registry
                .requestMatchers("/api/**", "/login", "/assets/**", "/favicon.ico", "/error").permitAll()
                .anyRequest().authenticated());

        http.cors(Customizer.withDefaults())
                // Form login handles the redirect to the login page from the authorization server filter chain
                .formLogin(Customizer.withDefaults());
//                .formLogin(configurer -> configurer
//                        .successHandler(fancyLoginSuccessHandler)
//                        .failureHandler(fancyLoginFailureHandler)
//                );
        http.oauth2Login(Customizer.withDefaults())
//        http.oauth2Login(configurer -> configurer
//                        .successHandler(fancyLoginSuccessHandler)
//                        .failureHandler(fancyLoginFailureHandler))
                .oauth2Client(Customizer.withDefaults());
        http.exceptionHandling(configurer -> configurer
                .authenticationEntryPoint(fancyAuthenticationEntryPoint));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
