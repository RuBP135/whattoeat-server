package com.rubp.whattoeat.server.feature.account.model;

import com.rubp.whattoeat.server.core.validation.ByteSize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CompleteRegistrationRequest(
        @NotBlank @ByteSize(min = 22, max = 22) @Pattern(regexp = "[A-Za-z0-9_-]+")
        String sessionCode,
        @NotBlank @Size(max = 255) @Pattern(regexp = "[^@]*") String username,
        @NotBlank String password
) { }
