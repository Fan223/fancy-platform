package fan.fancy.datasource.context;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

/**
 * 数据源上下文.
 *
 * @author Fan
 */
public class DataSourceContextHolder {

    // 使用 ThreadLocal 维护一个线程安全的上下文栈, Deque 支持数据源的嵌套压栈/弹栈
    private static final ThreadLocal<Deque<String>> CONTEXT_STACK = ThreadLocal.withInitial(ArrayDeque::new);

    private DataSourceContextHolder() {
    }

    /**
     * 将指定数据源压入当前线程的上下文栈顶.
     *
     * @param datasource 数据源标识
     */
    public static void push(String datasource) {
        Objects.requireNonNull(datasource, "datasource must not be null");
        CONTEXT_STACK.get().push(datasource);
    }

    /**
     * 获取当前线程使用的数据源标识(栈顶元素), 不弹出.
     *
     * @return {@link String}
     */
    public static String peek() {
        return CONTEXT_STACK.get().peek();
    }

    /**
     * 弹出当前线程上下文栈顶的数据源，并在栈为空时彻底清除 ThreadLocal.
     */
    public static void poll() {
        Deque<String> deque = CONTEXT_STACK.get();
        deque.poll();
        if (deque.isEmpty()) {
            clear();
        }
    }

    /**
     * 清除当前线程的上下文栈, 彻底移除 ThreadLocal 中的数据, 避免内存泄漏.
     */
    public static void clear() {
        CONTEXT_STACK.remove();
    }
}
