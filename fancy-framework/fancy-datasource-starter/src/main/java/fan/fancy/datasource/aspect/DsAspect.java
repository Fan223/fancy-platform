package fan.fancy.datasource.aspect;

import fan.fancy.datasource.annotation.FancyDs;
import fan.fancy.datasource.context.DataSourceContextHolder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;

/**
 * 数据源切面.
 *
 * @author Fan
 */
@Aspect
public class DsAspect {

    @Around("@within(fan.fancy.datasource.annotation.FancyDs) || @annotation(fan.fancy.datasource.annotation.FancyDs)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 解析数据源注解, 没有注解则继续执行原方法
        FancyDs ds = resolveDs(joinPoint);
        if (ds == null) {
            return joinPoint.proceed();
        }

        // 有注解则将数据源标识压入上下文, 执行原方法, 最后弹出数据源标识
        try {
            DataSourceContextHolder.push(ds.value());
            return joinPoint.proceed();
        } finally {
            DataSourceContextHolder.poll();
        }
    }

    private FancyDs resolveDs(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Class<?> targetClass = joinPoint.getTarget().getClass();

        // 获取具体的方法(解决接口/代理/泛型桥接问题)
        Method method = ClassUtils.getMostSpecificMethod(signature.getMethod(), targetClass);
        // 优先从方法上找注解
        FancyDs ds = AnnotatedElementUtils.findMergedAnnotation(method, FancyDs.class);
        if (ds != null) {
            return ds;
        }
        // 方法上没有再从类上找
        return AnnotatedElementUtils.findMergedAnnotation(targetClass, FancyDs.class);
    }
}
