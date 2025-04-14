package com.github.netroforge.authronom_backend.config;

import com.github.netroforge.authronom_backend.repository.redis.OAuth2AuthorizationGrantAuthorizationRepository;
import com.github.netroforge.authronom_backend.repository.redis.OAuth2RegisteredClientRepository;
import com.github.netroforge.authronom_backend.repository.redis.OAuth2UserConsentRepository;
import com.github.netroforge.authronom_backend.repository.redis.RedisRegisteredClientRepository;
import com.github.netroforge.authronom_backend.service.RedisOAuth2AuthorizationConsentService;
import com.github.netroforge.authronom_backend.service.RedisOAuth2AuthorizationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.convert.RedisCustomConversions;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import java.util.Arrays;

@Configuration
public class RedisConfig {

    // Configure Spring Session to use Redis
    @Bean
    public RedisOperations<String, Object> redisOperations(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        return template;
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new JedisConnectionFactory();
    }

    @Bean
    public RedisTemplate<?, ?> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<byte[], byte[]> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

//    @Bean
//    public RedisCustomConversions redisCustomConversions() {
//        return new RedisCustomConversions(
//                Arrays.asList(
//                        new UsernamePasswordAuthenticationTokenToBytesConverter(),
//                        new BytesToUsernamePasswordAuthenticationTokenConverter(),
//                        new OAuth2AuthorizationRequestToBytesConverter(),
//                        new BytesToOAuth2AuthorizationRequestConverter(),
//                        new ClaimsHolderToBytesConverter(),
//                        new BytesToClaimsHolderConverter()
//                )
//        );
//    }

    @Bean
    public RedisRegisteredClientRepository registeredClientRepository(
            OAuth2RegisteredClientRepository registeredClientRepository) {
        return new RedisRegisteredClientRepository(registeredClientRepository);
    }

    @Bean
    public RedisOAuth2AuthorizationService authorizationService(
            RegisteredClientRepository registeredClientRepository,
            OAuth2AuthorizationGrantAuthorizationRepository authorizationGrantAuthorizationRepository
    ) {
        return new RedisOAuth2AuthorizationService(registeredClientRepository,
                authorizationGrantAuthorizationRepository);
    }

    @Bean
    public RedisOAuth2AuthorizationConsentService authorizationConsentService(
            OAuth2UserConsentRepository userConsentRepository
    ) {
        return new RedisOAuth2AuthorizationConsentService(userConsentRepository);
    }
}
