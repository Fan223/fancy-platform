package fan.fancy.starter.log.aspect;

import fan.fancy.starter.log.annotation.FancyLog;
import fan.fancy.starter.log.event.FancyLogEvent;
import fan.fancy.starter.log.printer.FancyLogPrinter;
import fan.fancy.starter.log.properties.FancyLogProperties;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * Controller 日志切面.
 *
 * @author Fan
 */
@Aspect
public class FancyControllerLogAspect {

    private final FancyLogProperties properties;

    public FancyControllerLogAspect(FancyLogProperties properties) {
        this.properties = properties;
    }

    @Around("within(@org.springframework.web.bind.annotation.RestController *) || within(@org.springframework.stereotype.Controller *)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        FancyLog fancyLog = AnnotatedElementUtils.findMergedAnnotation(method, FancyLog.class);
        // 如果方法或类上存在 @FancyLog 注解则跳过
        if (fancyLog != null || !properties.isEnabled() || !properties.getController().isEnabled()) {
            return joinPoint.proceed();
        }

        long start = System.nanoTime();
        Object result = null;
        Throwable exception = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable ex) {
            exception = ex;
            throw ex;
        } finally {
            long costMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            publishEvent(joinPoint, result, exception, costMs);
        }
    }

    /**
     * 发布日志事件.
     *
     * @param joinPoint {@link ProceedingJoinPoint}
     * @param result    返回结果
     * @param exception {@link Throwable}
     * @param costMs    耗时(毫秒)
     */
    private void publishEvent(ProceedingJoinPoint joinPoint, Object result, Throwable exception, long costMs) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        FancyLogEvent event = FancyLogEvent.builder()
                .serviceName(properties.getServiceName())
                .className(signature.getDeclaringTypeName())
                .methodName(signature.getName())
                .args(properties.isPrintArgs() ? joinPoint.getArgs() : null)
                .maxArgsLength(properties.getMaxArgsLength())
                .result(properties.isPrintResult() ? result : null)
                .maxResultLength(properties.getMaxResultLength())
                .costMs(costMs)
                .exception(exception)
                .build();
        FancyLogPrinter.print(event);
    }
}
