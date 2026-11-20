package com.rubp.whattoeat.server.feature.account.model;

public record StartRegistrationResponse(
        VerificationType verificationType,
        String sessionCode
) { }
