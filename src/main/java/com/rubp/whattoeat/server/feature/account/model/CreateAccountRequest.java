package com.rubp.whattoeat.server.feature.account.model;

import jakarta.validation.constraints.NotBlank;

public record CreateAccountRequest(
        @NotBlank String username,
        @NotBlank String password
) { }
