package com.rubp.whattoeat.server.feature.account.model;

import com.rubp.whattoeat.server.core.validation.ByteSize;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StartEmailRegistrationRequest(
        @NotBlank @Size(min = 22, max = 22) String invitationCode,
        @NotBlank @Email @ByteSize(max = 254) String email
) { }
