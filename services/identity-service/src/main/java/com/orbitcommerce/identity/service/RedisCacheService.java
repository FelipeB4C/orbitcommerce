package com.orbitcommerce.identity.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class RedisCacheService {

    private final StringRedisTemplate redisTemplate;

    public RedisCacheService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void set(String key, String value, long ttlSeconds) {
        this.redisTemplate.opsForValue().set(key, value, ttlSeconds, TimeUnit.SECONDS);
    }

    public String get(String key) {
        return this.redisTemplate.opsForValue().get(key);
    }

    public Boolean hasKey(String key) {
        return this.redisTemplate.hasKey(key);
    }

    public Optional<String> getOptional(String key) {
        return Optional.ofNullable(this.redisTemplate.opsForValue().get(key));
    }

    public boolean del(String key) {
        return Boolean.TRUE.equals(this.redisTemplate.delete(key));
    }


}
