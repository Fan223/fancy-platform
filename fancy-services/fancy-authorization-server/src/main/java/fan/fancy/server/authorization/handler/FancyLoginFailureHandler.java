package fan.fancy.server.authorization.handler;

import fan.fancy.toolkit.http.Response;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author Fan
 */
@Slf4j
@AllArgsConstructor
public class FancyLoginFailureHandler implements AuthenticationFailureHandler {

    private final JsonMapper jsonMapper;

    @Override
    public void onAuthenticationFailure(@NonNull HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        String message = exception.getMessage();
        log.error("FancyLoginFailureHandler: {}", message);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8);

        jsonMapper.writeValue(response.getOutputStream(), Response.fail(HttpServletResponse.SC_FORBIDDEN, message));
    }
}
