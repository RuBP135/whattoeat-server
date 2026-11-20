package com.rubp.whattoeat.server.feature.account.model;

public record StartRegistrationRequest(
        String invitationCode,
        String email
) { }
