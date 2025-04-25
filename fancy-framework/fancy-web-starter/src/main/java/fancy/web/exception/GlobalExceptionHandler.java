package fancy.web.exception;

import fancy.toolkit.net.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器.
 *
 * @author Fan
 * @since 2025/4/22 16:13
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public Response<String> handleOtherException(Exception e) {
        LOGGER.error("系统异常: {}", e.getMessage(), e);
        return Response.fail("系统异常: " + e.getMessage(), null);
    }
}
