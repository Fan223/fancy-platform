package fan.fancy.starter.client.admin.bootstrap;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * 监控客户端环境后置处理器.
 *
 * @author Fan
 */
public class FancyAdminClientEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, @NonNull SpringApplication application) {
        Map<String, Object> properties = new HashMap<>();
        properties.putIfAbsent("spring.boot.admin.client.url", "http://localhost:12000");
        properties.putIfAbsent("spring.boot.admin.client.instance.service-host-type", "IP");
        properties.putIfAbsent("management.endpoints.web.exposure.include", "*");

        PropertySource<?> propertySource = new MapPropertySource("fancyAdminClient", properties);
        environment.getPropertySources().addLast(propertySource);
    }
}
