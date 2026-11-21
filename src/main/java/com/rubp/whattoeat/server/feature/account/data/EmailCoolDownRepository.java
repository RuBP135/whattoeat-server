package com.rubp.whattoeat.server.feature.account.data;

import com.rubp.whattoeat.server.core.codec.Base64UrlCodec;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Objects;

@Repository
public class EmailCoolDownRepository {

    private static final String KEY_PREFIX = "email:cooldown:";

    private final StringRedisTemplate redis;

    public EmailCoolDownRepository(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public boolean tryStartCooldown(byte[] emailHmac) {
        String key = KEY_PREFIX + Base64UrlCodec.encode(emailHmac);
        Boolean created = redis.opsForValue().setIfAbsent(key, "", Duration.ofSeconds(60));
        return Objects.equals(created, true);
    }
}
