package fan.fancy.server.authorization.handler;

import fan.fancy.server.authorization.util.WebUtils;
import fan.fancy.toolkit.http.Response;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author Fan
 */
@Slf4j
@AllArgsConstructor
@Component
public class FancyLoginFailureHandler implements AuthenticationFailureHandler {

    private final JsonMapper jsonMapper;

    private final AuthenticationFailureHandler authenticationFailureHandler = new SimpleUrlAuthenticationFailureHandler("/login?error");

    @Override
    public void onAuthenticationFailure(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        String message = exception.getMessage();
        log.error("FancyLoginFailureHandler: {}", message);

        if (WebUtils.isBrowser(request)) {
            authenticationFailureHandler.onAuthenticationFailure(request, response, exception);
        } else {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8);
            jsonMapper.writeValue(response.getOutputStream(), Response.fail(HttpServletResponse.SC_FORBIDDEN, message));
        }
    }
}
