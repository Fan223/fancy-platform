package fan.fancy.starter.server.resource.reactive.handler;

import fan.fancy.toolkit.http.Response;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * {@link ConditionalOnWebApplication.Type#REACTIVE} 拒绝访问处理器.
 *
 * @author Fan
 */
@Slf4j
@AllArgsConstructor
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class FancyReactiveAccessDeniedHandler implements ServerAccessDeniedHandler {

    private final JsonMapper jsonMapper;

    @Override
    public @NonNull Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException denied) {
        String message = denied.getMessage();
        log.error("FancyReactiveAccessDeniedHandler: {}", message);

        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);

        HttpHeaders headers = response.getHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAcceptCharset(List.of(StandardCharsets.UTF_8));

        byte[] body = jsonMapper.writeValueAsBytes(Response.fail(HttpStatus.FORBIDDEN.value(), message));
        DataBuffer buffer = response.bufferFactory().wrap(body);
        return response.writeWith(Mono.just(buffer));
    }
}
