package fancy.web.config;

import fancy.web.interceptor.FancyInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置类.
 *
 * @author Fan
 * @since 2023/7/10
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 注册拦截器.
     */
    @Bean
    public FancyInterceptor fancyInterceptor() {
        return new FancyInterceptor();
    }

    /**
     * 添加拦截器.
     *
     * @param registry {@link InterceptorRegistry}
     * @author Fan
     * @since 2025/6/17 14:37
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册拦截器
        registry.addInterceptor(fancyInterceptor())
                // 拦截所有请求
                .addPathPatterns("/**")
                // 排除不需要拦截的路径
                .excludePathPatterns("/api");
    }
}