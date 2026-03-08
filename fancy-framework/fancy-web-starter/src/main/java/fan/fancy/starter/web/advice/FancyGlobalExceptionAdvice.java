package fan.fancy.starter.web.advice;

import fan.fancy.toolkit.http.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理.
 *
 * @author Fan
 */
@ControllerAdvice
@RestControllerAdvice
public class FancyGlobalExceptionAdvice {

    private static final Logger LOGGER = LoggerFactory.getLogger(FancyGlobalExceptionAdvice.class);

    @ExceptionHandler(Exception.class)
    public Response<String> handleOtherException(Exception exception) {
        LOGGER.error("系统异常: {}", exception.getMessage(), exception);
        return Response.fail("系统异常: " + exception.getMessage());
    }
}
