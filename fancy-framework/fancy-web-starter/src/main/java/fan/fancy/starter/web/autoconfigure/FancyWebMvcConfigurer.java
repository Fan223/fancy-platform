package fan.fancy.starter.web.autoconfigure;

import fan.fancy.starter.web.interceptor.FancyHandlerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置.
 *
 * @author Fan
 */
public class FancyWebMvcConfigurer implements WebMvcConfigurer {

    @Bean
    public FancyHandlerInterceptor fancyHandlerInterceptor() {
        return new FancyHandlerInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 添加拦截器
        registry.addInterceptor(fancyHandlerInterceptor())
                // 拦截所有请求
                .addPathPatterns("/**")
                // 排除不需要拦截的路径
                .excludePathPatterns("/api");
    }
}
