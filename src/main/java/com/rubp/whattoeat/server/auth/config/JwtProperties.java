package com.rubp.whattoeat.server.auth.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties (
        @NotBlank String privateKeyBase64,
        @NotBlank String publicKeyBase64,
        @NotNull Duration accessTokenTtl,
        @NotNull Duration refreshTokenTtl
) { }
