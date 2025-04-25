package fancy.log.autoconfigure;

import fancy.log.aspect.ControllerLogAspect;
import fancy.log.aspect.FancyLogAspect;
import fancy.log.filter.TraceIdMdcFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

/**
 * 日志自动配置类.
 *
 * @author Fan
 * @since 2025/4/17 14:05
 */
@AutoConfiguration
public class LogAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public FancyLogAspect fancyLogAspect() {
        return new FancyLogAspect();
    }

    @Bean
    @ConditionalOnMissingBean
    public ControllerLogAspect controllerLogAspect() {
        return new ControllerLogAspect();
    }

    @Bean
    public FilterRegistrationBean<TraceIdMdcFilter> traceIdMdcFilter() {
        // 注册 TraceIdMdcFilter
        FilterRegistrationBean<TraceIdMdcFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new TraceIdMdcFilter());
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        // 指定拦截路径
        registration.addUrlPatterns("/*");
        return registration;
    }
}
