package fancy.redis.autoconfigure;

import fancy.redis.core.RedisClient;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 自动配置类.
 *
 * @author Fan
 * @since 2025/3/6 15:04
 */
@AutoConfiguration
@ConditionalOnClass({RedissonClient.class, RedisTemplate.class})
@EnableConfigurationProperties(RedisProperties.class)
public class RedisAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RedissonClient redissonClient(RedisProperties properties) {
        Config config = new Config();
        config.setCodec(new JsonJacksonCodec());

        String address = String.format("redis://%s:%d", properties.getHost(), properties.getPort());
        config.useSingleServer()
                .setAddress(address)
                .setPassword(properties.getPassword())
                .setDatabase(properties.getDatabase());
        return Redisson.create(config);
    }

    @Bean
    @ConditionalOnMissingBean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // 字符串序列化
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        // 通用 JSON 序列化
        GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer = new GenericJackson2JsonRedisSerializer();

        // 显式设置 Key:Value 的序列化类型
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setValueSerializer(genericJackson2JsonRedisSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        redisTemplate.setHashValueSerializer(genericJackson2JsonRedisSerializer);

        // 设置默认 Key:Value 序列化类型
        redisTemplate.setDefaultSerializer(genericJackson2JsonRedisSerializer);
        // 初始化
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    @ConditionalOnBean({RedissonClient.class, RedisTemplate.class})
    @ConditionalOnMissingBean
    public RedisClient redisClient(RedissonClient redissonClient, RedisTemplate<String, Object> redisTemplate) {
        return new RedisClient(redissonClient, redisTemplate);
    }
}
