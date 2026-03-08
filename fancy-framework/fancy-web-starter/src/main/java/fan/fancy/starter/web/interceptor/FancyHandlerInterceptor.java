package fan.fancy.starter.web.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * 拦截器.
 *
 * @author Fan
 */
public class FancyHandlerInterceptor implements HandlerInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(FancyHandlerInterceptor.class);

    /**
     * 在请求处理之前调用, Controller 方法调用之前.
     *
     * @param request  {@link HttpServletRequest}
     * @param response {@link HttpServletResponse}
     * @param handler  {@link Object}
     * @return {@code boolean}
     */
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        LOGGER.info("拦截请求: {}", request.getRequestURI());
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    /**
     * 请求处理之后但视图被渲染之前调用, Controller 方法调用之后.
     *
     * @param request      {@link HttpServletRequest}
     * @param response     {@link HttpServletResponse}
     * @param handler      {@link Object}
     * @param modelAndView {@link ModelAndView}
     */
    @Override
    public void postHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, @Nullable ModelAndView modelAndView) throws Exception {
        LOGGER.info("请求处理完成");
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    /**
     * 在整个请求结束之后被调用, 也就是在 DispatcherServlet 渲染了对应的视图之后, 主要用于资源清理工作.
     *
     * @param request  {@link HttpServletRequest}
     * @param response {@link HttpServletResponse}
     * @param handler  {@link Object}
     * @param ex       {@link Exception}
     */
    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, @Nullable Exception ex) throws Exception {
        LOGGER.info("请求完全结束");
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
