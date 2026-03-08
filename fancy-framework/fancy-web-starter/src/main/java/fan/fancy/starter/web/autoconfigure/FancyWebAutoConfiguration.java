package fan.fancy.starter.web.autoconfigure;

import fan.fancy.starter.web.advice.FancyGlobalExceptionAdvice;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;


/**
 * Web 自动配置类.
 *
 * @author Fan
 */
@AutoConfiguration
public class FancyWebAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public FancyGlobalExceptionAdvice fancyGlobalExceptionAdvice() {
        return new FancyGlobalExceptionAdvice();
    }
}
