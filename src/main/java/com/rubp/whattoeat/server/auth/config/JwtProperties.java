package com.rubp.whattoeat.server.auth.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Valid
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties (
        @NotBlank String privateKeyBase64,
        @NotBlank String publicKeyBase64,
        @NotNull @Positive Duration accessTokenTtl,
        @NotNull @Positive Duration refreshTokenTtl
) { }
