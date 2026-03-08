package fan.fancy.starter.log.filter;

import fan.fancy.toolkit.id.IdUtils;
import fan.fancy.toolkit.lang.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.ThreadContext;
import org.jspecify.annotations.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * TraceId 过滤器.
 *
 * @author Fan
 */
public class FancyTraceIdFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-Trace-Id";

    private static final String TRACE_ID = "traceId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String traceId = request.getHeader(HEADER);
        if (StringUtils.isBlank(traceId)) {
            traceId = IdUtils.generateSnowflakeIdStr();
        }

        try {
            ThreadContext.put(TRACE_ID, traceId);
            response.setHeader(HEADER, traceId);
            filterChain.doFilter(request, response);
        } finally {
            ThreadContext.remove(TRACE_ID);
        }
    }
}
