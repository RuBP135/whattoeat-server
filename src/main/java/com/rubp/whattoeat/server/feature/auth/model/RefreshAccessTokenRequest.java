package com.rubp.whattoeat.server.feature.auth.model;

import jakarta.validation.constraints.NotBlank;

public record RefreshAccessTokenRequest(
        @NotBlank String refreshToken
) { }
