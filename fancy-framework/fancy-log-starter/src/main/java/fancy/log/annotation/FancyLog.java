package fancy.log.annotation;

import java.lang.annotation.*;

/**
 * 日志注解.
 *
 * @author Fan
 * @since 2025/4/17 13:46
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FancyLog {

    /**
     * 日志所属模块.
     */
    String module() default "";
}
