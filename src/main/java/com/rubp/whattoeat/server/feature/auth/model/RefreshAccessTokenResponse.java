package com.rubp.whattoeat.server.feature.auth.model;

public record RefreshAccessTokenResponse(
        String accessToken,
        String refreshToken
) {
}
