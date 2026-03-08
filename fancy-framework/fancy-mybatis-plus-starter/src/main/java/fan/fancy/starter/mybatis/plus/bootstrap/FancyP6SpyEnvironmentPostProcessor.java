package fan.fancy.starter.mybatis.plus.bootstrap;

import fan.fancy.starter.mybatis.plus.properties.FancyP6SpyProperties;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * P6Spy 环境后置处理器.
 *
 * @author Fan
 */
public class FancyP6SpyEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, @NonNull SpringApplication application) {
        Map<String, Object> properties = new HashMap<>();
        properties.putIfAbsent("decorator.datasource.p6spy.log-format", FancyP6SpyProperties.LOG_FORMAT);

        PropertySource<?> propertySource = new MapPropertySource("fancyP6Spy", properties);
        environment.getPropertySources().addLast(propertySource);
    }
}
