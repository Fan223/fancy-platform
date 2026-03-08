package fan.fancy.starter.validation.advice;

import fan.fancy.toolkit.http.Response;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

/**
 * 参数校验异常处理.
 *
 * @author Fan
 */
@ControllerAdvice
@RestControllerAdvice
public class FancyValidationExceptionAdvice {

    private static final Logger LOGGER = LoggerFactory.getLogger(FancyValidationExceptionAdvice.class);

    /**
     * {@link ConstraintViolationException} 处理, 简单参数校验.
     *
     * @param exception {@link ConstraintViolationException}
     * @return {@link Response}
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Response<String> handleConstraintViolationException(ConstraintViolationException exception) {
        String message = exception.getConstraintViolations().iterator().next().getMessage();
        LOGGER.error("简单参数校验失败: {}", message, exception);
        return Response.fail("简单参数校验失败: " + message);
    }

    /**
     * {@link MethodArgumentNotValidException} 处理, 实体类参数校验.
     *
     * @param exception {@link MethodArgumentNotValidException}
     * @return {@link Response}
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Response<String> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        String message = "实体类参数校验失败: ";
        FieldError fieldError = exception.getBindingResult().getFieldError();
        if (fieldError != null) {
            message += fieldError.getDefaultMessage();
        }
        LOGGER.error(message, exception);
        return Response.fail(message);
    }

    /**
     * {@link BindException} 处理, 表单参数绑定校验.
     *
     * @param exception {@link BindException}
     * @return {@link Response}
     */
    @ExceptionHandler(BindException.class)
    public Response<String> handleBindException(BindException exception) {
        String message = "表单参数绑定失败: ";
        FieldError fieldError = exception.getFieldError();
        if (fieldError != null) {
            message += fieldError.getDefaultMessage();
        }
        LOGGER.error(message, exception);
        return Response.fail(message);
    }

    /**
     * {@link HandlerMethodValidationException} 处理, 方法参数校验.
     *
     * @param exception {@link HandlerMethodValidationException}
     * @return {@link Response}
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public Response<String> handleMethodValidationException(HandlerMethodValidationException exception) {
        String message = exception.getAllErrors().getFirst().getDefaultMessage();
        LOGGER.error("方法参数校验失败: {}", message, exception);
        return Response.fail("方法参数校验失败: " + message);
    }
}
