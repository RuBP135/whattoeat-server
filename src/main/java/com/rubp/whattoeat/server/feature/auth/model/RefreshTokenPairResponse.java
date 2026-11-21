package com.rubp.whattoeat.server.feature.auth.model;

public record RefreshTokenPairResponse(
        String accessToken,
        String refreshToken
) {
}
