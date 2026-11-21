package com.rubp.whattoeat.server.feature.account.model;

public record CompleteRegistrationResponse(
        String accessToken,
        String refreshToken
) { }
