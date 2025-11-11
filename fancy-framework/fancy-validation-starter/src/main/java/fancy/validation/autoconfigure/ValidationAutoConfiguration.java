package fancy.validation.autoconfigure;

import fancy.validation.exception.ValidationExceptionHandler;
import fancy.validation.service.ValidatorService;
import jakarta.validation.Validator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * 参数校验自动配置类.
 *
 * @author Fan
 * @since 2025/4/24 17:07
 */
@AutoConfiguration
public class ValidationAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ValidationExceptionHandler validationExceptionHandler() {
        return new ValidationExceptionHandler();
    }

    @Bean
    @ConditionalOnBean(Validator.class)
    @ConditionalOnMissingBean
    public ValidatorService validatorService(Validator validator) {
        return new ValidatorService(validator);
    }
}
