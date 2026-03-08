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
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * {@link FancyLog} 切面.
 *
 * @author Fan
 */
@Aspect
public class FancyLogAspect {

    private final FancyLogProperties properties;

    public FancyLogAspect(FancyLogProperties properties) {
        this.properties = properties;
    }

    @Around("@within(fan.fancy.starter.log.annotation.FancyLog) || @annotation(fan.fancy.starter.log.annotation.FancyLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        FancyLog fancyLog = resolveFancyLog(joinPoint);
        if (fancyLog == null || !properties.isEnabled() || !properties.getAnnotation().isEnabled()) {
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
            publishEvent(joinPoint, fancyLog, result, exception, costMs);
        }
    }

    /**
     * 解析 {@link FancyLog} 注解.
     *
     * @param joinPoint {@link ProceedingJoinPoint}
     * @return {@link FancyLog}
     */
    private FancyLog resolveFancyLog(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Class<?> targetClass = joinPoint.getTarget().getClass();
        // 获取具体的方法(解决接口/代理/泛型桥接问题)
        Method method = ClassUtils.getMostSpecificMethod(signature.getMethod(), targetClass);
        return AnnotatedElementUtils.findMergedAnnotation(method, FancyLog.class);
    }

    /**
     * 发布日志事件.
     *
     * @param joinPoint {@link ProceedingJoinPoint}
     * @param fancyLog  {@link FancyLog}
     * @param result    返回结果
     * @param exception {@link Throwable}
     * @param costMs    耗时(毫秒)
     */
    private void publishEvent(ProceedingJoinPoint joinPoint, FancyLog fancyLog, Object result, Throwable exception, long costMs) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        FancyLogEvent event = FancyLogEvent.builder()
                .serviceName(properties.getServiceName())
                .className(signature.getDeclaringTypeName())
                .methodName(signature.getName())
                .tag(fancyLog.value())
                .args(fancyLog.printArgs() ? joinPoint.getArgs() : null)
                .maxArgsLength(fancyLog.maxArgsLength())
                .result(fancyLog.printResult() ? result : null)
                .maxResultLength(fancyLog.maxResultLength())
                .costMs(costMs)
                .exception(exception)
                .build();
        FancyLogPrinter.print(event);
    }
}
