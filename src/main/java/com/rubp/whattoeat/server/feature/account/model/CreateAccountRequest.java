package com.rubp.whattoeat.server.feature.account.model;

public record CreateAccountRequest(
        String username,
        String password
) { }
