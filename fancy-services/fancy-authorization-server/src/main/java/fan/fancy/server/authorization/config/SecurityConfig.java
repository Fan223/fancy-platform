package fan.fancy.server.authorization.config;

import fan.fancy.server.authorization.handler.FancyAuthenticationEntryPoint;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http.authorizeHttpRequests(registry -> registry
                .requestMatchers("/api/**", "/login", "/assets/**", "/error").permitAll()
                .anyRequest().authenticated());

        http.cors(Customizer.withDefaults())
                // Form login handles the redirect to the login page from the authorization server filter chain
                .formLogin(Customizer.withDefaults());

        http.exceptionHandling(configurer -> configurer
                .authenticationEntryPoint(fancyAuthenticationEntryPoint));
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails userDetails = User.withDefaultPasswordEncoder()
                .username("user")
                .password("111111")
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(userDetails);
    }
}
