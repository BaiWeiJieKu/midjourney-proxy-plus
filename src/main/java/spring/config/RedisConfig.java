package spring.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.TimeoutOptions;
import io.lettuce.core.protocol.ProtocolVersion;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * redis配置类
 *
 * @author xugao
 */
@Configuration
public class RedisConfig extends CachingConfigurerSupport {

    /**
     * redis集群配置
     * https://github.com/lettuce-io/lettuce-core/issues/1201
     * https://github.com/lettuce-io/lettuce-core/issues/1543
     *
     * @return @return org.springframework.boot.autoconfigure.data.redis.LettuceConnectionConfiguration#createClientOptions()
     * @author qinfen
     * @date 2022/10/26
     */
    @Bean
    public LettuceClientConfigurationBuilderCustomizer redisBuilderCustomizer() {
        return builder -> builder.clientOptions(
                ClientOptions
                        .builder()
                        .protocolVersion(ProtocolVersion.RESP2)
                        .timeoutOptions(TimeoutOptions.enabled())
                        .build()
        );
    }

}
