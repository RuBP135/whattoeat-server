package com.rubp.whattoeat.server.feature.auth.config;

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
                    "Access token TTL must be greater than zero"
            );
        }

        if(refreshTokenTtl != null && !refreshTokenTtl.isPositive()){
            throw new IllegalStateException(
                    "Refresh token TTL must be greater than zero"
            );
        }
    }
}
