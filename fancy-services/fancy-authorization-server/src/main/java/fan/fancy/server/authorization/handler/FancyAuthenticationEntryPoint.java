package fan.fancy.server.authorization.handler;

import fan.fancy.server.authorization.util.WebUtils;
import fan.fancy.toolkit.http.Response;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 认证入口点, 处理未认证访问时的异常.
 *
 * @author Fan
 */
@Component
@Slf4j
public class FancyAuthenticationEntryPoint extends LoginUrlAuthenticationEntryPoint {

    private final JsonMapper jsonMapper;

    public FancyAuthenticationEntryPoint(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
        super("/login");
    }

    @Override
    public void commence(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        String message = authException.getMessage();
        log.error("FancyAuthenticationEntryPoint: {}", message);

        // 判断请求是否来自浏览器, 如果是浏览器则重定向到登录页面, 否则返回 JSON 格式的错误信息
        if (WebUtils.isBrowser(request)) {
            log.info("text");
            super.commence(request, response, authException);
        } else {
            log.info("json");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8);
            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, message);

            jsonMapper.writeValue(response.getOutputStream(), Response.fail(message));
        }
    }
}
