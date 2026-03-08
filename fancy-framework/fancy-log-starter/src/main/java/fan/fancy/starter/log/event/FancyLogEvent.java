package fan.fancy.starter.log.event;

import lombok.Builder;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.Objects;

/**
 * 日志事件.
 *
 * @author Fan
 */
@Builder
public record FancyLogEvent(
        // 服务名
        String serviceName,
        // 类全限定名
        String className,
        // 方法名
        String methodName,
        // 标签
        String tag,
        // 参数
        Object[] args,
        // 最大参数长度
        int maxArgsLength,
        // 返回结果
        Object result,
        // 最大返回结果长度
        int maxResultLength,
        // 耗时(毫秒)
        long costMs,
        // 异常对象
        Throwable exception
) {
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FancyLogEvent(
                String serviceName1, String className1, String methodName1, String tag1, Object[] args1, int argsLength,
                Object result1, int resultLength, long ms, Throwable exception1
        ))) {
            return false;
        }
        return maxArgsLength == argsLength && maxResultLength == resultLength && costMs == ms
                && Objects.equals(serviceName, serviceName1) && Objects.equals(className, className1)
                && Objects.equals(methodName, methodName1) && Objects.equals(tag, tag1)
                && Objects.deepEquals(args, args1) && Objects.equals(result, result1)
                && Objects.equals(exception, exception1);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceName, className, methodName, tag, Arrays.hashCode(args), maxArgsLength,
                result, maxResultLength, costMs, exception);
    }

    @Override
    @NonNull
    public String toString() {
        return "FancyLogEvent{" +
                "serviceName='" + serviceName + '\'' +
                ", className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                ", tag='" + tag + '\'' +
                ", args=" + Arrays.deepToString(args) +
                ", maxArgsLength=" + maxArgsLength +
                ", result=" + result +
                ", maxResultLength=" + maxResultLength +
                ", costMs=" + costMs +
                ", exception=" + exception +
                '}';
    }
}
