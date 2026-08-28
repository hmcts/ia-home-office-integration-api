package uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.config;

import io.lettuce.core.RedisURI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.client.model.idam.UserInfo;
import uk.gov.hmcts.reform.iahomeofficeintegrationapi.infrastructure.security.AesEncryptingRedisSerializer;

import java.time.Duration;

@EnableCaching
@Configuration
@ConditionalOnProperty(
    name = "app.cache.enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class CacheConfiguration {

    private static final Logger log = LoggerFactory.getLogger(CacheConfiguration.class);

    @Value("${spring.data.redis.encryption.key}") // Base64-encoded 32-byte key
    private String redisEncryptionKey;

    @Bean
    @Primary
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        try {
            redisConnectionFactory.getConnection().ping();
            log.info("Redis connection successful - using Redis for systemTokenCache");

            RedisCacheConfiguration userInfoCacheConfig = getCacheConfig(3300, UserInfo.class);
            RedisCacheConfiguration tokenCacheConfig = getCacheConfig(3300, String.class);
            RedisCacheConfiguration hoTokenCacheConfig = getCacheConfig(90, String.class);

            return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(tokenCacheConfig)
                .withCacheConfiguration("systemUserTokenCache", tokenCacheConfig)
                .withCacheConfiguration("hoTokenCache", hoTokenCacheConfig)
                .withCacheConfiguration("userInfoCache", userInfoCacheConfig)
                // caches for functional tests
                .withCacheConfiguration("legalRepATokenCache", tokenCacheConfig)
                .withCacheConfiguration("caseOfficerTokenCache", tokenCacheConfig)
                .withCacheConfiguration("adminOfficerTokenCache", tokenCacheConfig)
                .withCacheConfiguration("homeOfficePouTokenCache", tokenCacheConfig)
                .withCacheConfiguration("homeOfficeGenericTokenCache", tokenCacheConfig)
                .withCacheConfiguration("judgeTokenCache", tokenCacheConfig)
                .build();

        } catch (Exception e) {
            // if redis is down, dont cache make idam calls, until pod restarts
            log.warn("Redis unavailable: {}", e.getMessage());
            return new NoOpCacheManager();
        }
    }

    private <T> RedisCacheConfiguration getCacheConfig(int time, Class<T> valueType) {
        AesEncryptingRedisSerializer<T> tokenSerializer =
            new AesEncryptingRedisSerializer<>(
                new Jackson2JsonRedisSerializer<>(valueType),
                redisEncryptionKey
            );
        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofSeconds(time))
            .disableCachingNullValues()
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair
                    .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair
                    .fromSerializer(tokenSerializer));
    }
    @Bean
    public RedisConnectionFactory redisConnectionFactory(
        @Value("${spring.data.redis.url}") String redisUrl,
        @Value("${spring.data.redis.secret}") String accessKey) {
        try {

            if (redisUrl == null || redisUrl.isBlank()) {
                log.warn("No Redis URL configured");
                // return a dummy factory - cacheManager will catch the ping failure and fall back
                return new LettuceConnectionFactory();
            }

            RedisURI redisUri = RedisURI.create(redisUrl);

            boolean useSsl = redisUrl.contains("tls=true") || redisUrl.startsWith("rediss://");

            // checked azure portal,
            if (useSsl) {
                redisUri.setSsl(true);
                redisUri.setVerifyPeer(false); // for Azure (self signed certs)
            }

            redisUri.setTimeout(Duration.ofSeconds(10)); // 64seconds is default, so fail quicker

            RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
            config.setHostName(redisUri.getHost());
            config.setPort(redisUri.getPort());
            if (accessKey != null && !accessKey.isBlank()) {
                config.setPassword(RedisPassword.of(accessKey));
            }

            LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .commandTimeout(Duration.ofSeconds(5))
                .useSsl()
                .disablePeerVerification()
                .build();

            LettuceConnectionFactory factory = new LettuceConnectionFactory(
                config,
                clientConfig
            );

            factory.afterPropertiesSet();
            log.info("Successful Redis connection.");
            return factory;
        } catch (Exception e) {
            log.error("Failed to create Redis connection factory: {}", e.getMessage());
            throw e;
        }
    }

}
