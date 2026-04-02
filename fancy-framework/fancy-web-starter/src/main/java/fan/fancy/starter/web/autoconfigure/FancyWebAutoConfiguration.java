package fan.fancy.starter.web.autoconfigure;

import fan.fancy.starter.web.advice.FancyGlobalExceptionAdvice;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;


/**
 * Web 自动配置类.
 *
 * @author Fan
 */
@AutoConfiguration
public class FancyWebAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public FancyGlobalExceptionAdvice fancyGlobalExceptionAdvice() {
        return new FancyGlobalExceptionAdvice();
    }

    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
