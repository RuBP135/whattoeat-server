package com.rubp.whattoeat.server.feature.account.model;

import com.rubp.whattoeat.server.core.validation.ByteSize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyRegistrationRequest(
        @NotBlank @ByteSize(min = 22, max = 22) @Pattern(regexp = "[A-Za-z0-9_-]+")
        String sessionCode,
        @NotBlank @Pattern(regexp = "[0-9]{6}") String verificationCode
) { }
