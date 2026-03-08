package fan.fancy.starter.server.resource.autoconfigure.servlet;

import fan.fancy.starter.server.resource.servlet.authorize.FancyAuthorizeCustomizer;
import fan.fancy.starter.server.resource.servlet.configurer.FancyResourceServerConfigurer;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * {@link ConditionalOnWebApplication.Type#SERVLET} 资源服务器自动配置类.
 *
 * @author Fan
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableWebSecurity
@EnableMethodSecurity
@AllArgsConstructor
public class FancyResourceServerAutoConfiguration {

    private ObjectProvider<FancyAuthorizeCustomizer> authorizeCustomizers;

    @Bean
    @ConditionalOnMissingBean
    public SecurityFilterChain defaultResourceServerSecurityFilterChain(HttpSecurity http,
                                                                        FancyResourceServerConfigurer resourceServerConfigurer) {
        // 应用资源服务器配置
        http.apply(resourceServerConfigurer);
        http.authorizeHttpRequests(registry -> {
            // 应用所有自定义的授权配置器
            authorizeCustomizers.orderedStream()
                    .forEach(customizer -> customizer.customize(registry));
            // 其他请求都需要认证
            registry.anyRequest().authenticated();
        });
        return http.build();
    }

    @Bean
    public FancyAuthorizeCustomizer defaultAuthorizeCustomizer() {
        // 默认授权配置器, 放行 "/api" 路径下的所有请求
        return registry -> registry.requestMatchers("/api/**").permitAll();
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtAuthenticationConverter defaultJwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        // 设置解析权限信息的前缀, 为空则是去掉前缀
        grantedAuthoritiesConverter.setAuthorityPrefix("");
        // 设置权限信息在 Access Token Claims 中的 Key, 从而解析获取权限信息
        grantedAuthoritiesConverter.setAuthoritiesClaimName("authorities");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }
}
