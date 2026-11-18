package com.rubp.whattoeat.server.feature.account.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAccountRequest(
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank @Size(min = 22, max = 22) String invitationCode
) { }
