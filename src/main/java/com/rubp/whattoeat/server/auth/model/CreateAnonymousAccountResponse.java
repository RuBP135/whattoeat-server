package com.rubp.whattoeat.server.auth.model;

public record CreateAnonymousAccountResponse(
        String accessToken,
        String refreshToken,
        String uid
) { }
