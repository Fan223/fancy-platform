package fan.fancy.server.authorization.handler;

import fan.fancy.toolkit.http.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 认证接口异常处理.
 *
 * @author Fan
 */
@Slf4j
@RestControllerAdvice(basePackages = "fan.fancy.server.authorization.controller")
public class AuthUserExceptionAdvice {

    @ExceptionHandler(IllegalStateException.class)
    public Response<Void> handleConflict(IllegalStateException ex) {
        log.warn("auth conflict: {}", ex.getMessage());
        return Response.fail(409, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Response<Void> handleBadRequest(IllegalArgumentException ex) {
        log.warn("auth bad request: {}", ex.getMessage());
        return Response.badRequest(ex.getMessage());
    }
}