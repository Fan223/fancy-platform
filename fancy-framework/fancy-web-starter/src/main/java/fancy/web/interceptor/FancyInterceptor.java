package fancy.web.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * 自定义拦截器.
 *
 * @author Fan
 * @since 2023/7/10
 */
public class FancyInterceptor implements HandlerInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(FancyInterceptor.class);

    /**
     * 在请求处理之前调用, Controller 方法调用之前.
     *
     * @param request  {@link HttpServletRequest}
     * @param response {@link HttpServletResponse}
     * @param handler  {@link Object}
     * @return {@code boolean}
     * @author Fan
     * @since 2025/6/17 14:31
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        LOGGER.info("拦截请求: {}", request.getRequestURI());
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    /**
     * 请求处理之后视图被渲染之前调用, Controller 方法调用之后.
     *
     * @param request      {@link HttpServletRequest}
     * @param response     {@link HttpServletResponse}
     * @param handler      {@link Object}
     * @param modelAndView {@link ModelAndView}
     * @author Fan
     * @since 2025/6/17 14:32
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
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
     * @author Fan
     * @since 2025/6/17 14:34
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        LOGGER.info("请求完全结束");
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}