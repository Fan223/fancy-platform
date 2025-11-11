package fancy.monitor.processor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * 默认配置后处理器.
 *
 * @author Fan
 * @since 2025/6/18 13:35
 */
@Configuration
public class FancyMonitorEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        // 创建默认配置的属性源
        Map<String, Object> defaults = new HashMap<>();
        // Spring Boot Admin Client 配置
        defaults.put("spring.boot.admin.client.url", "http://localhost:8080");
        // Management Endpoints 配置
        defaults.put("management.endpoints.web.exposure.include", "*");
        // 创建属性源, 添加到环境中，使用addLast确保用户配置优先级更高
        environment.getPropertySources().addLast(new MapPropertySource("fancyMonitorDefaults", defaults));
    }
}