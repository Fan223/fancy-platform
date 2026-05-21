package fan.fancy.server.authorization.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign 配置类.
 *
 * @author Fan
 */
@Configuration
public class FeignConfig {

    @Value("${fancy.security.internal-token}")
    private String internalToken;

    @Bean
    public RequestInterceptor internalRequestInterceptor() {
        return new InternalRequestInterceptor(internalToken);
    }
}
