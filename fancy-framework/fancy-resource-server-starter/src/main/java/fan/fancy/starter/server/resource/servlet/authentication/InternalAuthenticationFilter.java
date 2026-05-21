package fan.fancy.starter.server.resource.servlet.authentication;

import fan.fancy.toolkit.lang.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 内部认证过滤器, 在 JWT 校验前运行. 当请求携带正确的 X-Internal-Token 时注入内部认证, 实现服务间内部调用免 JWT 认证.
 *
 * @author Fan
 */
public class InternalAuthenticationFilter extends OncePerRequestFilter {

    private static final String INTERNAL_TOKEN_HEADER = "X-Internal-Token";

    private static final String INTERNAL_SERVICE = "internal-service";

    @Value("${fancy.security.internal-token}")
    private String internalToken;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String requestToken = request.getHeader(INTERNAL_TOKEN_HEADER);
        if (StringUtils.isNotBlank(requestToken) && requestToken.equals(internalToken)) {
            InternalAuthenticationToken internalAuthenticationToken = new InternalAuthenticationToken(INTERNAL_SERVICE);
            SecurityContextHolder.getContext().setAuthentication(internalAuthenticationToken);
        }
        filterChain.doFilter(request, response);
    }
}
