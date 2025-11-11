package fancy.validation.service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

import java.util.Set;

/**
 * 参数手动校验.
 *
 * @author Fan
 * @since 2025/4/24 17:28
 */
public class ValidatorService {

    private final Validator validator;

    public ValidatorService(Validator validator) {
        this.validator = validator;
    }

    /**
     * 手动校验.
     *
     * @param obj    对象
     * @param groups 分组
     * @author Fan
     * @since 2025/4/25 9:45
     */
    public <T> void validate(T obj, Class<?>... groups) {
        Set<ConstraintViolation<T>> validate = validator.validate(obj, groups);
        if (!validate.isEmpty()) {
            throw new ConstraintViolationException("参数手动校验失败: " + validate.iterator().next().getMessage(), validate);
        }
    }
}
