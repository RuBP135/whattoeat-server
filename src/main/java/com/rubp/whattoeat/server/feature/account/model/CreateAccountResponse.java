package com.rubp.whattoeat.server.feature.account.model;

public record CreateAccountResponse(
        String accessToken,
        String refreshToken
) { }
