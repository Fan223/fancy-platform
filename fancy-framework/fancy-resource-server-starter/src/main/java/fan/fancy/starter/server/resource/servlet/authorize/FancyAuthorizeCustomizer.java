package fan.fancy.starter.server.resource.servlet.authorize;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

/**
 * 自定义 {@link ConditionalOnWebApplication.Type#SERVLET} 授权配置器..
 *
 * @author Fan
 */
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public interface FancyAuthorizeCustomizer extends Customizer<AuthorizeHttpRequestsConfigurer<HttpSecurity>
        .AuthorizationManagerRequestMatcherRegistry> {
}
