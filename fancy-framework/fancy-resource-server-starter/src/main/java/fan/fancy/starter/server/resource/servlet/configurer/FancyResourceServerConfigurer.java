package fan.fancy.starter.server.resource.servlet.configurer;

import fan.fancy.starter.server.resource.servlet.handler.FancyAccessDeniedHandler;
import fan.fancy.starter.server.resource.servlet.handler.FancyAuthenticationEntryPoint;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

/**
 * 资源服务器配置器.
 *
 * @author Fan
 */
@AllArgsConstructor
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class FancyResourceServerConfigurer extends AbstractHttpConfigurer<FancyResourceServerConfigurer, HttpSecurity> {

    private final FancyAuthenticationEntryPoint authenticationEntryPoint;

    private final FancyAccessDeniedHandler accessDeniedHandler;

    @Override
    public void init(HttpSecurity http) {
        http.oauth2ResourceServer(configurer -> configurer
                .jwt(Customizer.withDefaults())
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
        );
    }
}
