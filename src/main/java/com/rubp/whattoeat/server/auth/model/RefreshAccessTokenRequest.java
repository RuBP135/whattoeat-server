package com.rubp.whattoeat.server.auth.model;

import jakarta.validation.constraints.NotBlank;

public record RefreshAccessTokenRequest(
        @NotBlank String refreshToken
) { }
