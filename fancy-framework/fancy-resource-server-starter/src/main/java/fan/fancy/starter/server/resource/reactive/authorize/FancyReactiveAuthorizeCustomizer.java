package fan.fancy.starter.server.resource.reactive.authorize;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;

/**
 * 自定义 {@link ConditionalOnWebApplication.Type#REACTIVE} 授权配置器.
 *
 * @author Fan
 */
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public interface FancyReactiveAuthorizeCustomizer extends Customizer<ServerHttpSecurity.AuthorizeExchangeSpec> {
}