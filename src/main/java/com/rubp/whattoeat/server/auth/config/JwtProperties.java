package com.rubp.whattoeat.server.auth.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties (
        @NotNull Resource privateKeyLocation,
        @NotNull Resource publicKeyLocation,
        @NotNull Duration accessTokenTtl,
        @NotNull Duration refreshTokenTtl
) {
    public JwtProperties {
        if(accessTokenTtl != null && !accessTokenTtl.isPositive()){
            throw new IllegalStateException(
                    "access token ttl 必须大于零"
            );
        }

        if(refreshTokenTtl != null && !refreshTokenTtl.isPositive()){
            throw new IllegalStateException(
                    "refresh token ttl 必须大于零"
            );
        }
    }
}
