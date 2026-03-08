package fan.fancy.starter.server.resource.autoconfigure.reactive;

import fan.fancy.starter.server.resource.reactive.authorize.FancyReactiveAuthorizeCustomizer;
import fan.fancy.starter.server.resource.reactive.configurer.FancyReactiveResourceServerConfigurer;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

/**
 * {@link ConditionalOnWebApplication.Type#REACTIVE} 资源服务器自动配置类..
 *
 * @author Fan
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@AllArgsConstructor
public class FancyReactiveResourceServerAutoConfiguration {

    private ObjectProvider<FancyReactiveAuthorizeCustomizer> authorizeCustomizers;

    @Bean
    @ConditionalOnMissingBean
    SecurityWebFilterChain defaultReactiveResourceServerSecurityFilterChain(ServerHttpSecurity http,
                                                                            FancyReactiveResourceServerConfigurer resourceServerConfigurer) {
        // 应用资源服务器配置
        resourceServerConfigurer.configure(http);
        http.authorizeExchange(spec -> {
            // 应用所有自定义的授权配置器
            authorizeCustomizers.orderedStream()
                    .forEach(customizer -> customizer.customize(spec));
            // 其他请求都需要认证
            spec.anyExchange().authenticated();
        });
        return http.build();
    }

    @Bean
    public FancyReactiveAuthorizeCustomizer defaultReactiveAuthorizeCustomizer() {
        // 默认授权配置器, 放行 "/api" 路径下的所有请求
        return spec -> spec.pathMatchers("/api/**").permitAll();
    }

    @Bean
    @ConditionalOnMissingBean
    public Converter<Jwt, Mono<AbstractAuthenticationToken>> defaultReactiveJwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        // 设置解析权限信息的前缀, 为空则是去掉前缀
        grantedAuthoritiesConverter.setAuthorityPrefix("");
        // 设置权限信息在 Access Token Claims 中的 Key, 从而解析获取权限信息
        grantedAuthoritiesConverter.setAuthoritiesClaimName("authorities");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter);
    }
}
