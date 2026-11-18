package com.rubp.whattoeat.server.feature.auth.model;

public record LoginResponse(
        String accessToken,
        String refreshToken
) {
}
