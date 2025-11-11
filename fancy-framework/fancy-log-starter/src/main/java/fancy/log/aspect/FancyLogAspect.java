package fancy.log.aspect;

import fancy.log.annotation.FancyLog;
import org.aspectj.lang.ProceedingJoinPoint;
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
 * {@link FancyLog} 切面.
 *
 * @author Fan
 * @since 2025/4/17 13:48
 */
@Aspect
@Order(1)
public class FancyLogAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(FancyLogAspect.class);

    @Around("execution(* *(..)) && (within(@fancy.log.annotation.FancyLog *) || @annotation(fancy.log.annotation.FancyLog))")
    public Object fancyLogMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        FancyLog fancyLog = resolveFancyLog(joinPoint);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            return joinPoint.proceed();
        } finally {
            stopWatch.stop();
            String methodName = joinPoint.getSignature().toShortString();
            String args = Arrays.toString(joinPoint.getArgs());
            LOGGER.info("{} 执行方法: {}, 耗时: {}ms, 参数: {}", fancyLog.module(), methodName, stopWatch.getTotalTimeMillis(), args);
        }
    }

    /**
     * 解析 {@link FancyLog} 注解.
     *
     * @param joinPoint {@link ProceedingJoinPoint}
     * @return {@link FancyLog}
     * @author Fan
     * @since 2025/4/21 16:26
     */
    private FancyLog resolveFancyLog(ProceedingJoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        FancyLog fancyLog = method.getAnnotation(FancyLog.class);
        if (null == fancyLog) {
            fancyLog = joinPoint.getTarget().getClass().getAnnotation(FancyLog.class);
        }
        return fancyLog;
    }
}
