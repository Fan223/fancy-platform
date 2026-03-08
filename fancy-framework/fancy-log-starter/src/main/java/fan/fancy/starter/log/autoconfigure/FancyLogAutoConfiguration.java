package fan.fancy.starter.log.autoconfigure;

import fan.fancy.starter.log.aspect.FancyControllerLogAspect;
import fan.fancy.starter.log.aspect.FancyLogAspect;
import fan.fancy.starter.log.filter.FancyTraceIdFilter;
import fan.fancy.starter.log.properties.FancyLogProperties;
import fan.fancy.toolkit.lang.StringUtils;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

/**
 * 日志自动配置类.
 *
 * @author Fan
 */
@AutoConfiguration
@EnableConfigurationProperties(FancyLogProperties.class)
public class FancyLogAutoConfiguration {

    private final FancyLogProperties properties;

    @Value("${spring.application.name:}")
    private String applicationName;

    public FancyLogAutoConfiguration(FancyLogProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void init() {
        if (StringUtils.isBlank(properties.getServiceName()) && StringUtils.isNotBlank(applicationName)) {
            properties.setServiceName(applicationName);
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public FancyControllerLogAspect fancyControllerLogAspect() {
        return new FancyControllerLogAspect(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public FancyLogAspect fancyLogAspect() {
        return new FancyLogAspect(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public FilterRegistrationBean<FancyTraceIdFilter> fancyTraceIdFilter() {
        // 注册 TraceIdFilter
        FilterRegistrationBean<FancyTraceIdFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new FancyTraceIdFilter());
        // 指定拦截路径
        bean.addUrlPatterns("/*");
        bean.setDispatcherTypes(DispatcherType.REQUEST);
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }
}
