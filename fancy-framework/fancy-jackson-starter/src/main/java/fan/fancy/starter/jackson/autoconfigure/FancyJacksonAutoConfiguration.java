package fan.fancy.starter.jackson.autoconfigure;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.json.JsonMapper;

/**
 * Jackson 自动配置类.
 *
 * @author Fan
 */
@AutoConfiguration
@ConditionalOnClass(JsonMapper.class)
public class FancyJacksonAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JsonMapper jsonMapper() {
        return JsonMapper.builder()
                // 忽略 isGetter 方法
                .changeDefaultVisibility(vc -> vc
                        .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE))
                // 忽略 null 值
                .changeDefaultPropertyInclusion(incl -> incl
                        .withValueInclusion(JsonInclude.Include.NON_NULL)
                        .withContentInclusion(JsonInclude.Include.NON_NULL))
                .build();
    }
}
