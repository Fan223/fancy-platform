package fancy.log.aspect;

import fancy.log.annotation.FancyLog;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.util.StopWatch;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 控制器切面.
 *
 * @author Fan
 * @since 2025/4/17 16:38
 */
@Aspect
@Order(2)
public class ControllerLogAspect {

    private static final Logger log = LoggerFactory.getLogger(ControllerLogAspect.class);

    @Around("within(@org.springframework.web.bind.annotation.RestController *)")
    public Object controllerLogMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        Signature signature = joinPoint.getSignature();
        Method method = ((MethodSignature) signature).getMethod();
        // 如果方法或类上存在 @FancyLog 注解则跳过
        if (method.isAnnotationPresent(FancyLog.class)
                || method.getDeclaringClass().isAnnotationPresent(FancyLog.class)) {
            return joinPoint.proceed();
        }

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            return joinPoint.proceed();
        } finally {
            stopWatch.stop();
            String methodName = signature.toShortString();
            String args = Arrays.toString(joinPoint.getArgs());
            log.info("执行方法: {}, 耗时: {}ms, 参数: {}", methodName, stopWatch.getTotalTimeMillis(), args);
        }
    }
}
