package fan.fancy.starter.redis.autoconfigure;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJackson3Codec;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.data.redis.autoconfigure.DataRedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.json.JsonMapper;


/**
 * Redis 自动配置类.
 *
 * @author Fan
 */
@AutoConfiguration
@ConditionalOnClass({RedissonClient.class, RedisTemplate.class})
@EnableConfigurationProperties(DataRedisProperties.class)
public class FancyRedisAutoConfiguration {

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean
    public RedissonClient redissonClient(DataRedisProperties properties, JsonMapper jsonMapper) {
        Config config = new Config();
        config.setUsername(properties.getUsername());
        config.setPassword(properties.getPassword());
        config.setCodec(new JsonJackson3Codec(jsonMapper));

        String address = String.format("redis://%s:%d", properties.getHost(), properties.getPort());
        config.useSingleServer()
                .setAddress(address)
                .setDatabase(properties.getDatabase());
        return Redisson.create(config);
    }

    @Bean
    @ConditionalOnMissingBean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory, JsonMapper jsonMapper) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 字符串序列化
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        // 通用 JSON 序列化
        GenericJacksonJsonRedisSerializer jsonSerializer = new GenericJacksonJsonRedisSerializer(jsonMapper);

        // 设置 Key:Value 的序列化类型
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(jsonSerializer);

        // 初始化
        template.afterPropertiesSet();
        return template;
    }
}
