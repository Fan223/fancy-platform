package fan.fancy.starter.log.annotation;

import java.lang.annotation.*;

/**
 * 日志注解.
 *
 * @author Fan
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FancyLog {

    /**
     * 描述信息.
     */
    String value() default "";

    /**
     * 是否打印参数.
     */
    boolean printArgs() default true;

    /**
     * 最大参数长度.
     */
    int maxArgsLength() default 2048;

    /**
     * 是否打印返回结果.
     */
    boolean printResult() default true;

    /**
     * 最大返回结果长度.
     */
    int maxResultLength() default 2048;
}
