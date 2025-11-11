package fancy.web.autoconfigure;

import fancy.web.config.WebMvcConfig;
import fancy.web.exception.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * Web 自动配置类.
 *
 * @author Fan
 * @since 2025/4/22 16:15
 */
@AutoConfiguration
@ConditionalOnWebApplication
@Import(WebMvcConfig.class)
public class WebAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }
}
