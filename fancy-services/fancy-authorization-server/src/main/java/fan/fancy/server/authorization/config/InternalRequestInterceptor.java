package fan.fancy.server.authorization.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;

/**
 * 内部请求拦截器, 向下游服务注入 X-Internal-Token.
 *
 * @author Fan
 */
public class InternalRequestInterceptor implements RequestInterceptor {

    public static final String INTERNAL_TOKEN_HEADER = "X-Internal-Token";

    private final String internalToken;

    InternalRequestInterceptor(String internalToken) {
        this.internalToken = internalToken;
    }

    @Override
    public void apply(RequestTemplate template) {
        template.header(INTERNAL_TOKEN_HEADER, internalToken);
    }
}
