package fancy.validation.exception;

import fancy.toolkit.net.Response;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 参数校验异常处理器.
 *
 * @author Fan
 * @since 2025/4/24 17:08
 */
@RestControllerAdvice
public class ValidationExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationExceptionHandler.class);

    /**
     * 参数校验异常 {@link MethodArgumentNotValidException}.
     *
     * @param e {@link MethodArgumentNotValidException}
     * @return {@link Response<String>}
     * @author Fan
     * @since 2025/4/24 17:17
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Response<String> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String message = "参数校验失败: ";
        FieldError fieldError = e.getBindingResult().getFieldError();
        if (null != fieldError) {
            message += fieldError.getDefaultMessage();
        }
        LOGGER.error(message, e);
        return Response.fail(message);
    }

    /**
     * 参数校验异常 {@link ConstraintViolationException}.
     *
     * @param e {@link ConstraintViolationException}
     * @return {@link Response<String>}
     * @author Fan
     * @since 2025/4/24 17:17
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Response<String> handleConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations().iterator().next().getMessage();
        LOGGER.error("参数校验失败: {}", message, e);
        return Response.fail("参数校验失败: " + message);
    }
}
