package fancy.log.filter;

import fancy.toolkit.id.IdUtils;
import jakarta.servlet.*;
import org.apache.logging.log4j.ThreadContext;

import java.io.IOException;

/**
 * 为每个请求添加 traceId 并放入 MDC.
 *
 * @author Fan
 * @since 2025/4/17 14:03
 */
public class TraceIdMdcFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            ThreadContext.put("traceId", IdUtils.getSnowflakeIdStr());
            chain.doFilter(request, response);
        } finally {
            ThreadContext.clearMap();
        }
    }
}