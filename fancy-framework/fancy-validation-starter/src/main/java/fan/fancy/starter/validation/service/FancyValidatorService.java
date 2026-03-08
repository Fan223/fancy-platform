package fan.fancy.starter.validation.service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

import java.util.Set;

/**
 * 参数校验服务.
 *
 * @author Fan
 */
public class FancyValidatorService {

    private final Validator validator;

    public FancyValidatorService(Validator validator) {
        this.validator = validator;
    }

    public <T> void validate(T obj, Class<?>... groups) {
        Set<ConstraintViolation<T>> validate = validator.validate(obj, groups);
        if (!validate.isEmpty()) {
            throw new ConstraintViolationException("参数校验失败: " + validate.iterator().next().getMessage(), validate);
        }
    }
}
