package com.evaluation.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "spring.redis.host", matchIfMissing = false)
public class RedisConfig {
    // Redis configuration is optional - app works without it
}
